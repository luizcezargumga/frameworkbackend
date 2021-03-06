package io.gumga.domain.service;

import java.io.Serializable;
import java.util.List;

/**
 * Service com a operação de delete
 */
//@FunctionalInterface
public interface GumgaDeletableServiceable<T, ID extends Serializable> {

    public void delete(T resource);
    
    public void delete(List<T> resource);
    

}
