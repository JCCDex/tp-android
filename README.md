# About TP-Android

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)

This is a wallet tool based on [TokenPocket](https://www.mytokenpocket.vip) early version for android, we could use it to manage multiple wallets, view balance & transaction history and transfer token. Only supports [SWTC](http://state.jingtum.com/#!/), [Ethereum](https://etherscan.io/) and [Moac](http://explorer.moac.io/home) for now. For Ethereum and Moac, we only support transfer eth & moac except erc20.

这是一个基于TokenPocket早期版本的Android钱包工具，我们可以用它来管理多个钱包，查看余额和交易历史记录以及转账,此外也可以用塔进行 DAPP开发迭代。
目前仅支持SWTC公链, 以太坊和墨客. 对于以太坊和墨客, 仅支持eth和moac转账.未来将会添加墨客联盟链的支持.

## Donation

If you wanna donate token, we'll appreciate it.

如果您想捐赠Token，我们将非常感谢。

Official Address:

| Chain | Address/Account  |
| :-: |:-:|
| EOS | [tpopensource](https://eosflare.io/account/tpopensource) |
| SWTC | [jGj83Xe4GEyDKXBDrFBGXdMWHKTSy29XUp](http://state.jingtum.com/#!/wallet/jGj83Xe4GEyDKXBDrFBGXdMWHKTSy29XUp) |
| MOAC | [0x77e7b7b5ea39bf1103f191e58ef44b1f74ccef1b](http://explorer.moac.io/addr/0x77e7b7b5ea39bf1103f191e58ef44b1f74ccef1b) |

### Discovery module 

------

- You can use DAPP in [discovery module].
- Interface supporting [tokenpocket](https://github.com/TP-Lab/tp-js-sdk#MOAC) 
- Interface supporting [MetaMask](https://docs.metamask.io/guide/signing-data.html#signing-data-with-metamask) 
-  The information of DAPP list is in Apps.json Set in
-   The existing DAPP application list contains the test page, which can be directly click to test.

#### ps: 

​		metamask is going through a new round of version iteration, and the interface logic conflicts with the existing logic of the project. Currently, the adaptation of metamask is still in the development stage. But we can be sure that the completed API and functions have covered all the requirements that meet the normal opening of metamask DAPP, obtaining the account number and sending RPC. If there is any third-party adaptation problem, please initiate issue

### 发现模块

------

-  你可以在 [发现模块] 使用 DAPP。

-  支持TokenPocket的接口 [参考这里](https://github.com/TP-Lab/tp-js-sdk#MOAC)。

-  支持MetaMask的接口 [参考这里](https://docs.metamask.io/guide/signing-data.html#signing-data-with-metamask)。

-  DAPP列表的信息在 Apps.json 中设置

- 现有 dapp 应用列表包含了测试页面, 可以直接点击进行测试。

  

附言 : 

​		MetaMask 正在经历新的一轮版本迭代 , 并且部接口逻辑和项目现有逻辑冲突。 现行对MetaMask的适配还处于开发阶段。但可以确定的是 已经完成的api和功能已经覆盖了所有符合MetaMask Dapp正常打开, 获取帐号,发送RPC的要求 。可以预见该功能需要大量的测试来完善,  如果您有任何第三方的适配问题 请发起 issue  。



## License

For more information see [License](https://github.com/TP-Lab/tp-android/blob/master/LICENSE)





