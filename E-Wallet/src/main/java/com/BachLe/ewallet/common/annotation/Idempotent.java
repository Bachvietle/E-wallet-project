package com.BachLe.ewallet.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @Target chỉ định Annotation này được dán ở đâu (METHOD nghĩa là dán trên các hàm của Controller)
@Target(ElementType.METHOD)
// @Retention chỉ định Annotation này tồn tại đến lúc nào (RUNTIME nghĩa là Spring có thể đọc nó lúc app đang chạy)
@Retention(RetentionPolicy.RUNTIME)

public @interface Idempotent {

}
