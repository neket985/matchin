enum class Month(val num: Int) {
    FEB(2),
    MAR(3),
    APR(4),
    MAY(5);

    companion object {
        private val numToMonth = Month.values().associateBy { it.num }
        fun getByNum(num: Int): Month? = numToMonth[num]
    }
}
