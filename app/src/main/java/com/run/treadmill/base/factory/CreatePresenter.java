package com.run.treadmill.base.factory;

import com.run.treadmill.base.BasePresenter;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @Description 标注创建Presenter的注解, 注意presenter的作用域
 * @Author GaleLiu
 * @Time 2019/05/29
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface CreatePresenter {

    Class<? extends BasePresenter> value();
}