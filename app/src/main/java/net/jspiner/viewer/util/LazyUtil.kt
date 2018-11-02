package net.jspiner.viewer.util

/*
Lazy로 선언될 변수를 명시적으로 생성해주기 위한 함수

ex)
val viewModel: ViewModel = by lazy { createViewModel(); }

override fun onCreate() {
    viewModel.initLazy()
}
*/
fun Any.initLazy() {
    //no-op
}