package com.example.commoncipher.operator;

public interface Operator<K> {

    /**
     * Operator的名称, 同一类型的Operator的路由器{@link OperatorRouter}能够根据该值路由到当前的Operator
     *
     * @return route key
     */
    K getName();

}