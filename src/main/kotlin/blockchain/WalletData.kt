package src.blockchain

import java.io.Serializable

class WalletData : Serializable {
    var Address: String? = null
    var CreatedAt: String? = null
    var Name: String? = null

    // is the wallet of a vault type
    var Vault = false

    // the limit amount. eg. 200dau per day
    var VaultLimit = 0f

    // date the vault is set to be open
    var VaultOpenDate: String? = null

    // limit time  // d = day, m = month, y = year
    var LimitTime: String? = null
}