# RAG 评测问题清单：订单与支付排障

本文档用于记录 Week 2 RAG 项目的人工评测问题。问题分为四类：直接命中、近义表达、无答案拒答、容易混淆。

## 直接命中

| 编号 | 问题 | 期望来源 |
| --- | --- | --- |
| Q01 | 支付中超过 5 分钟的订单应该怎么排查？ | `docs/sample-docs/order-status-definition.md` |
| Q02 | 已关闭订单还能继续支付吗？ | `docs/sample-docs/payment-timeout-handling.md` |
| Q03 | 用户支付成功但订单还是待支付，应该怎么处理？ | `docs/sample-docs/paid-order-not-updated.md` |
| Q04 | 未支付的已关闭订单需要退款吗？ | `docs/sample-docs/refund-basic-rules.md` |
| Q05 | 退款失败后可以直接重复提交退款吗？ | `docs/sample-docs/refund-failed-handling.md` |

## 近义表达

| 编号 | 问题 | 期望来源 |
| --- | --- | --- |
| Q06 | 客户说钱扣了但订单没变成已支付，要先查什么？ | `docs/sample-docs/paid-order-not-updated.md` |
| Q07 | 同一个订单付了两次，怎么判断是不是重复支付？ | `docs/sample-docs/duplicate-payment-handling.md` |
| Q08 | 用户觉得付多了，应该怎么核对实际付款金额？ | `docs/sample-docs/coupon-payment-amount.md` |
| Q09 | 订单关掉了但用户说已经付款，客服第一步要做什么？ | `docs/sample-docs/payment-timeout-handling.md` |

## 无答案拒答

| 编号 | 问题 | 期望来源 |
| --- | --- | --- |
| Q10 | 用户想修改收货地址，应该怎么处理？ | 无答案，应拒答或说明知识库未覆盖 |
| Q11 | 会员积分什么时候到账？ | 无答案，应拒答或说明知识库未覆盖 |
| Q12 | 商品发货后多久能送达？ | 无答案，应拒答或说明知识库未覆盖 |

## 容易混淆

| 编号 | 问题 | 期望来源 |
| --- | --- | --- |
| Q13 | 企业转账订单能不能按普通支付回调自动更新？ | `docs/sample-docs/payment-channel-difference.md` |
| Q14 | 用户提供付款截图，能不能直接人工改成已支付？ | `docs/sample-docs/manual-intervention-sop.md` |
| Q15 | 不同订单各有一笔支付成功，算不算重复支付？ | `docs/sample-docs/duplicate-payment-handling.md` |

