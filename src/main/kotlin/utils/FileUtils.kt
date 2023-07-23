package src.utils

import src.blockchain.WalletData
import java.io.*

class FileUtils {
    fun CreateFileObject(`object`: Serializable?, filename: String?): Boolean {
        try {
            //Saving of object in a file
            val file = FileOutputStream(filename)
            val out = ObjectOutputStream(file)

            // Method for serialization of object
            out.writeObject(`object`)
            out.close()
            file.close()

//            System.out.println("Object has been serialized");
        } catch (ex: IOException) {
            println("IOException is caught")
            return false
        }
        return true
    }

    fun ReadFileObj(filename: String?): Any? {
        val walletData: WalletData? = null
        var `object`: Any? = null
        try {
            // Reading the object from a file
            val file = FileInputStream(filename)
            val `in` = ObjectInputStream(file)

            // Method for deserialization of object
            `object` = `in`.readObject() as Any

//            in.close();
            file.close()

//            System.out.println("Object has been deserialized ");
        } catch (ex: IOException) {
            println("IOException is caught while reading data")
        } catch (ex: ClassNotFoundException) {
            println("ClassNotFoundException is caught")
        }
        return `object`
    }
}