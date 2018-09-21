package node.util.observer;

import node.util.observer.Observable;

public interface Observer<ObservedType>
{
    public void update(Observable<ObservedType> object, ObservedType data);
}