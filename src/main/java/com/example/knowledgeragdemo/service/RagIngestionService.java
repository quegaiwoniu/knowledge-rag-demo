package com.example.knowledgeragdemo.service;

import com.example.knowledgeragdemo.config.AppRagProperties;
import com.example.knowledgeragdemo.dto.RagDocumentMetadata;
import com.example.knowledgeragdemo.dto.RagIngestResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * RAG 文档导入服务。
 *
 * <p>这个服务是 Week 2 RAG 链路的第一步：把企业知识库 Markdown 文档读取进系统，
 * 并抽取后续检索、引用、排障需要的文档级元数据。</p>
 */
@Service
public class RagIngestionService {

    private final AppRagProperties ragProperties;

    /**
     * Day 9 先使用内存保存最近一次导入结果。
     *
     * <p>这样后续 Day 10 做 chunking 时，可以先基于内存文档继续演进，
     * 不必过早引入数据库。</p>
     */
    private final List<RagDocumentMetadata> importedDocuments = new CopyOnWriteArrayList<>();

    public RagIngestionService(AppRagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    /**
     * 执行一次完整的 Markdown 文档导入。
     *
     * <p>当前实现会重新扫描配置目录，并用本次扫描结果替换内存中的旧结果。
     * 这样每次调用 POST /rag/ingest 都可以理解为“刷新一次样例知识库”。</p>
     */
    public RagIngestResponse ingest() {
        Path sampleDocsDirectory = resolveSampleDocsDirectory();
        if (!Files.exists(sampleDocsDirectory) || !Files.isDirectory(sampleDocsDirectory)) {
            throw new IllegalStateException("sample docs directory does not exist: " + sampleDocsDirectory);
        }

        List<RagDocumentMetadata> documents = new ArrayList<>();

        // 用 contentHash 判断重复内容：即使两个文件名不同，只要内容完全一样，也只导入一份。
        Set<String> seenContentHashes = new HashSet<>();
        int duplicateCount = 0;

        for (Path markdownFile : listMarkdownFiles(sampleDocsDirectory)) {
            String content = readFile(markdownFile);
            String contentHash = sha256(content);

            if (!seenContentHashes.add(contentHash)) {
                // 重复文档不抛异常，因为企业知识库里偶尔会出现重复文件；这里选择跳过并统计。
                duplicateCount++;
                continue;
            }

            documents.add(new RagDocumentMetadata(
                    buildDocId(contentHash),
                    markdownFile.getFileName().toString(),
                    markdownFile.toString(),
                    extractTitle(markdownFile, content),
                    contentHash,
                    // Day 9 先记录本次导入时间；后续接数据库时可以改成持久化字段。
                    LocalDateTime.now()
            ));
        }

        // 当前是内存版 RAG，导入后替换最近一次结果，供 Day 10 切片服务继续读取。
        importedDocuments.clear();
        importedDocuments.addAll(documents);

        return new RagIngestResponse(documents.size(), duplicateCount, documents);
    }

    /**
     * 解析样例文档目录的真实位置。
     *
     * <p>这里兼容两种常见启动方式：</p>
     * <ul>
     *     <li>从后端项目根目录启动：knowledge-rag-demo/docs/sample-docs</li>
     *     <li>从父级工作区启动：ragdemo/knowledge-rag-demo/docs/sample-docs</li>
     * </ul>
     *
     * <p>如果配置的是绝对路径，则完全尊重配置值，方便以后部署到服务器或 Docker 时覆盖。</p>
     */
    private Path resolveSampleDocsDirectory() {
        Path configuredPath = Path.of(ragProperties.sampleDocsPath());
        if (configuredPath.isAbsolute()) {
            return configuredPath.normalize();
        }

        Path workingDirectory = getWorkingDirectory();

        // 场景 1：IDEA 或命令行的 working directory 就是 knowledge-rag-demo。
        Path directCandidate = workingDirectory.resolve(configuredPath).normalize();
        if (Files.exists(directCandidate)) {
            return directCandidate;
        }

        // 场景 2：IDEA 的 working directory 是父级 ragdemo，需要再进入后端项目目录。
        Path workspaceCandidate = workingDirectory
                .resolve("knowledge-rag-demo")
                .resolve(configuredPath)
                .normalize();
        if (Files.exists(workspaceCandidate)) {
            return workspaceCandidate;
        }

        // 两种位置都不存在时，返回第一候选路径，错误信息会直接告诉用户当前实际查找位置。
        return directCandidate;
    }

    /**
     * 获取应用启动时的工作目录。
     *
     * <p>生产环境使用 JVM 的 user.dir；测试中可以覆写这个方法，模拟 IDEA 从不同目录启动后端。</p>
     */
    protected Path getWorkingDirectory() {
        return Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    /**
     * 返回最近一次导入的文档元数据快照。
     *
     * <p>返回不可变副本，避免外部代码直接修改服务内部的内存状态。</p>
     */
    public List<RagDocumentMetadata> getImportedDocuments() {
        return List.copyOf(importedDocuments);
    }

    /**
     * 列出目录下所有 Markdown 文件。
     *
     * <p>按文件名排序是为了让接口返回结果稳定，测试和人工排查都会更容易。</p>
     */
    private List<Path> listMarkdownFiles(Path sampleDocsDirectory) {
        try (Stream<Path> files = Files.list(sampleDocsDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("failed to list sample docs directory: " + sampleDocsDirectory, e);
        }
    }

    /**
     * 用 UTF-8 读取 Markdown 文件。
     *
     * <p>项目里的样例语料包含中文内容，明确指定 UTF-8 可以避免不同电脑默认编码不一致。</p>
     */
    private String readFile(Path markdownFile) {
        try {
            return Files.readString(markdownFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read markdown file: " + markdownFile, e);
        }
    }

    /**
     * 从 Markdown 一级标题中提取文档标题。
     *
     * <p>如果文档没有一级标题，就退回使用文件名，保证 title 字段始终有值。</p>
     */
    private String extractTitle(Path markdownFile, String content) {
        return content.lines()
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).trim())
                .filter(title -> !title.isBlank())
                .findFirst()
                .orElse(markdownFile.getFileName().toString());
    }

    /**
     * 根据内容哈希生成稳定 docId。
     *
     * <p>这里没有使用随机 UUID，是为了让同一份文档重复导入时更容易追踪和对比。</p>
     */
    private String buildDocId(String contentHash) {
        return "doc-" + contentHash.substring(0, 12);
    }

    /**
     * 计算文档内容的 SHA-256。
     *
     * <p>contentHash 后续可以继续用于重复检测、增量索引和判断文档是否发生变化。</p>
     */
    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
