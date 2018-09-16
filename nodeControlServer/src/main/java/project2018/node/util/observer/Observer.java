package project2018.node.util.observer;

import project2018.node.util.observer.Observable;

public interface Observer<ObservedType>
{
    public void update(Observable<ObservedType> object, ObservedType data);
}