package node.util.observer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Observable<ObservedType>
{

	private List<Observer<ObservedType>> _observers = new LinkedList<Observer<ObservedType>>();

	public void addObserver(Observer<ObservedType> obs)
	{
		if (obs == null)
		{
			throw new IllegalArgumentException("Tried to add a null observer");
		}
		if (this._observers.contains(obs))
		{
			return;
		}
		this._observers.add(obs);
	}
	
	public void removeObserver(Observer<ObservedType> obs)
	{
		if (obs == null)
		{
			throw new IllegalArgumentException("Tried to add a null observer");
		}
		
		this._observers.remove(obs);
	}
	
	public int size()
	{
		return this._observers.size();
	}

	public void notifyObservers(ObservedType data)
	{
		for (Observer<ObservedType> obs : _observers)
		{
			obs.update(this, data);
		}
	}
	
	public void notifyObservers(ExecutorService pool, ObservedType data)
	{
		for (Observer<ObservedType> obs : _observers)
		{
			pool.submit(()->{obs.update(this, data);});
		}
	}
}