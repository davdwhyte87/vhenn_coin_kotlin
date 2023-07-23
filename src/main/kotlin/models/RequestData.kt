package src.models

class RequestData {
    var action: String? = null
    var WalletName: String? = null
    var walletAddress: String? = null
    lateinit var nodeServers: Array<String>
    var transfer = Transfer()
    var createWallet = CreateWallet()
    var balance: Balance = Balance()
    var newCoins = NewCoins()

     public inner class CreateWallet {
        var walletName: String? = null
        var walletAddress: String? = null
        var password: String? = null
        var isBroadcasted = false
        var createdAt: String? = null
        var isVault = false
        var vaultLimit = 0f
        var vaultOpenDate: String? = null
        var limitTime: String? = null
    }

    inner class NewCoins {
        var amount = 0f
        var password: String? = null
    }

    inner class Balance {
        var walletAddress: String? = null
    }

    inner class Transfer {
        var senderAddress: String? = null
        var receiverAddress: String? = null
        var amount = 0f
        var isBroadcasted = false
        var blockID: String? = null
        var senderPrivateKey:String? = null
    } //    public float Amount;
    //    public String SenderAddress;
}