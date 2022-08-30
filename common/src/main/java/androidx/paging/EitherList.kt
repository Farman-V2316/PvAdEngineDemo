package androidx.paging

class EitherList<T>(
        private val pagedList: PagedList<T?>? = null,
        private val simpleList: List<T?>? = null) {

    fun getList(): List<T?>? {
        if (pagedList != null) {
            return pagedList
        } else {
            return simpleList
        }
    }

    fun getSnapshot(): List<T?>? {
        if (pagedList != null) {
            return pagedList.snapshot()
        } else {
            return simpleList
        }
    }
}