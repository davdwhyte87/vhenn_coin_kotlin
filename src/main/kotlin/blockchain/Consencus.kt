package src.blockchain

import java.util.List

// this program helps nodes vote on what the true version of a certain data is.
//
class Consencus {
    inner class Vote {
        var `object`: Any? = null
        var Count: Int? = null
        override fun equals(obj: Any?): Boolean {

            // checking if the two objects
            // pointing to same object
            if (this === obj) return true

            // checking for two condition:
            // 1) object is pointing to null
            // 2) if the objects belong to
            // same class or not
            if (obj == null
                || this.javaClass != obj.javaClass
            ) return false
            val v1 = obj as Vote // type casting object to the
            // intended class type


            // checking if the two
            // objects share all the same values
            return `object` == v1.`object`
        }
    }

    fun Vote(items: Array<Any>): Vote {
        var votes = ArrayList<Vote>()
        for (item in items) {
            val vote = Vote()
            vote.`object` = item
            vote.Count = 1
            if (hasObject(item, votes.toTypedArray())) {
                votes = ArrayList(List.of(*addToVote(item, votes.toTypedArray())))
            } else {
                votes.add(vote)
            }
        }
        var highestVote = votes[0]
        for (vote in votes) {
            print(vote.Count)
            println(vote.`object`)
            if (highestVote.Count!! < vote.Count!!) {
                highestVote = vote
            }
        }
        return highestVote
    }

    fun addToVote(`object`: Any, votes: Array<Vote>): Array<Vote> {
        for (vote in votes) {
            if (vote.`object` == `object`) {
                vote.Count = vote.Count!! + 1
            }
        }
        return votes
    }

    fun hasObject(`object`: Any, votes: Array<Vote>): Boolean {
        // this checks if a vote object already exists
        for (vote in votes) {
            if (vote.`object` == `object`) {
                return true
            }
        }
        return false
    }
}