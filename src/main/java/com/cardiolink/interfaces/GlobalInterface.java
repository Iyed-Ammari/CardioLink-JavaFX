package com.cardiolink.interfaces;

import java.util.List;

public interface GlobalInterface<P> {
    void add(P p);
    void add2(P p);
    void update(P p);
    void delete(P p);
    List<P> getall();
}