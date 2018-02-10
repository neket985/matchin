enum class Month(val num: Int) {
    FEB(2),
    MAR(3),
    APR(4),
    MAY(5);

    companion object {
        fun getByNum(num: Int): Month? {
            for (i in Month.values()) {
                if (i.num == num) {
                    return i
                }
            }
            return null
        }
    }
}
