package src.blockchain

import src.models.RequestData
import src.models.RequestData.CreateWallet
import src.models.WalletSyncData
import src.utils.FileUtils
import src.utils.SecUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Wallet {
    var Address: String? = null
    var CreatedAt: String? = null
    var Name: String? = null
    var fileUtils: FileUtils = FileUtils()
    var secUtils: SecUtils = SecUtils()
    fun CreateWallet(walletReq: CreateWallet): String? {
        var walletAddress = ""
        //        String walletName = "";
        var tempName = ""

        // gnenrating wallet address
        val r = Random()
        for (i in 0..31) {
            val randomChar = (97 + r.nextInt(16)).toChar()
            tempName = tempName + randomChar
        }


        // if the client application (mobile app eg.) already generated an address, then there is no need to generate one
        if (walletReq.walletAddress == null) {
            // we get the wallet address by hashing the password 2times
            println("Generating wallet address")
            walletAddress = secUtils.GenerateHash(walletReq.password + walletReq.walletName+secUtils.GenerateRandString())!!

        } else {
            walletAddress = walletReq.walletAddress!!
        }


        if (IsWalletExists(walletAddress)) {
            println("Wallet exists ")
            return null
        }

        // create a folder in data
        val dir = File("data/$walletAddress/")
        dir.mkdirs()
        val walletData = WalletData()
        walletData.Address = walletAddress
        walletData.Name = walletReq.walletName
        if (walletReq.isVault) {
            walletData.Vault = true
            walletData.VaultLimit = walletReq.vaultLimit
            walletData.VaultOpenDate = walletReq.vaultOpenDate
            walletData.LimitTime = walletReq.limitTime
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z")
        val date = Date(System.currentTimeMillis())
        walletData.CreatedAt = formatter.format(date)
        fileUtils.CreateFileObject(walletData, "data/$walletAddress/data.bin")

        // create new blockchain data
        val blockchain = Blockchain()
        blockchain.NewChain(walletAddress, walletReq.password)


//        WalletData walletx = (WalletData) obj;
//        System.out.println("Read wallet address :"+walletx.Address);
//        System.out.println("Read wallet name :"+walletx.Name);
//        System.out.println("Read wallet time :"+walletx.CreatedAt);


        // broadcast wallet creation
        val requestData = RequestData()
//        val broadCast = BroadCast()
//        requestData.action = "CREATE_WALLET"
//        requestData.createWallet.isBroadcasted = true
//        requestData.createWallet.walletAddress = walletAddress
//        requestData.createWallet.walletName = walletReq.walletName
//        requestData.createWallet.password = walletReq.password
//        broadCast.BCreateWallet(requestData)
        println("returning wallet address")
        return walletAddress
    }

    fun IsWalletExists(WalletName: String): Boolean {
        val f = File("data/$WalletName/")
        return if (f.isDirectory) {
            true
        } else false
    }

    fun GetWallets(): Array<WalletSyncData> {
        val directoryPath = File("data/")
        val walletAddresses = directoryPath.list()
        val fileUtils = FileUtils()
        val walletSyncDatalist: ArrayList<WalletSyncData> = ArrayList<WalletSyncData>()
        assert(walletAddresses != null)
        for (address in walletAddresses!!) {
            val wallObj: Any? = fileUtils.ReadFileObj(address)
            val walletData: WalletData = wallObj as WalletData
            val walletSyncData = WalletSyncData()
            walletSyncData.WalletAddress = address
            walletSyncData.WalletData = walletData
            walletSyncDatalist.add(walletSyncData)
        }
        return walletSyncDatalist.toTypedArray<WalletSyncData>()
    }

    fun CreateMultipleWallets(wallets: Array<WalletSyncData?>) {
        val fileUtils = FileUtils()
        for (syncData in wallets) {
            // if wallet address does not exist
            if (!IsWalletExists(syncData?.WalletAddress!!)) {
                fileUtils.CreateFileObject(syncData.WalletData, syncData.WalletAddress)
            }
        }
    }

    fun IssueNewCoins(amount: Float, password: String?) {
        // this allows users to issue new tokens
        val WalletAddress = "new_issued"
        val walletData = WalletData()
        walletData.Address = WalletAddress
        walletData.Name = "New Mint"

        // create a folder in data
        val dir = File("data/$WalletAddress/")
        dir.mkdirs()
        val formatter = SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z")
        val date = Date(System.currentTimeMillis())
        walletData.CreatedAt = formatter.format(date)
        fileUtils.CreateFileObject(walletData, "data/$WalletAddress/data.bin")

        // create new blockchain data
        val blockchain = Blockchain()
        blockchain.Mint(amount, password, WalletAddress)
    }
}