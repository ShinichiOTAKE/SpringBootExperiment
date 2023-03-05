package jp.example.experiment.util;

import java.beans.IntrospectionException;

public interface BeanIntrospector {

    void introspect(IntrospectionContext icontext) throws IntrospectionException;
    
}
