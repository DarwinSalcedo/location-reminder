package com.udacity.project4.base

import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import com.example.android.core_impl.delegate.dataBinding

abstract class BaseActivity<T : ViewDataBinding>(@LayoutRes contentLayoutId: Int) :
    AppCompatActivity(contentLayoutId) {

    protected val binding: T by dataBinding()
}