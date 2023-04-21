# CAS-认证服务

### 1.介绍

- 一处登录，全服务认证
- 简化接入第三方登录流程,只需在CAS认证服务里接入第三方登录即可。
- 安全、快速

### 2.认证流程

#### 名词解释
```AS```
```Authentication Service```：认证服务，发放```TGT```

```KDC```
```Key Distribution Center```：密钥发放中心

```TGS```
```Ticket-Granting Service```：票据授权服务，索取```TGT```，发放```ST```

```TGC```
ticket-granting cookie：授权的票据证明，由```CAS Server```通过```set-cookie```方式发送给终端用户。该值存在Cookie中，根据```TGC```可以找到```TGT```。

```TGT```
```Ticket Granting tieckt```：俗称大令牌，或者票根，由```KDC```和```AS```发放，获取该票据后，可直接申请其他服务票据ST，不需要提供身份认证信息

```ST```
```Service Ticket```：服务票据，由```KDC```的```TGS```发放，```ST```是访问server内部的令牌



###### 当然，服务端流程被简化了，中小型项目够用了。TGT存在redis中，没有过期就是认证了，可以直接拿到ST

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9naXRlZS5jb20vc3VoYW93L25vdGUvcmF3L21hc3Rlci9pbWcvMjAyMDA2MDgxNDQ0MjgucG5n?x-oss-process=image/format,png)

```CAS认证```最后返回了```ST```,由服务的服务端去校验```ST```是否有效（15s内），有效则返回基础用户信息【有点像```oauth```味道了】，然后各服务只需按照自己的认证持久化进行即可，比如```session```、```JWT```等

### 3.接入流程

1. 只需判断在自己服务里是否为登录状态，没登录成功或者需要登录直接重定向到```http://192.168.12.122:55554?service=YOUR_URL```即可*[ip可能后续会改为10段]*
2. 登录完成，或者```cas认证```还未过期将直接回调service并且带上ticket参数[例：http://192.168.12.122:8081?ticket=xxxxxxxxxx]，这个ticket就是```ST```
3. 拿着ST请求自己后端，让后端拿着ST调用```/user/auth/sg```，会返回基础用户信息
4. 拿id匹配自己有无用户，然后返回自己持久化认证即完成认证了。



### 4.API文档

```前端回调```:需要CAS认证的地方跳转```http://192.168.12.122:55554?service=YOUR_URL```即可，YOUR_URL为回调地址，这个地址需要能处理返回的```ST```，把他交给服务端进行校验完成认证



| 地址                | 请求方式   | ```params/data```                 |
| ------------------- | ---------- | --------------------------------- |
| ```/user/auth/sg``` | ```POST``` | ```st```:就是回调后地址里的ticket |
|                     |            |                                   |
|                     |            |                                   |

! 内部环境里API请求地址为:

http://192.168.12.122:55555/ [可能后续变更为10段，无所谓了，后端也不存在跨域问题]