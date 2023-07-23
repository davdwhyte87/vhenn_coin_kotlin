package src.models

class CreateWalletReq {
    var WalletName: String? = null
    var WalletAddress: String? = null
    var WalletVault = false
    var Password: String? = null
    var VaultLimit = 0f
    var VaultOpenDate: String? = null

    // d = day, m = month, y = year
    // d == 100dau per day limit
    var LimitTime: String? = null
}