package node.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PublicThreadPool
{
	ExecutorService service = Executors.newCachedThreadPool();
}
