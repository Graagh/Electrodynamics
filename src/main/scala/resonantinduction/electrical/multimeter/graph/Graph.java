package resonantinduction.electrical.multimeter.graph;

import net.minecraft.nbt.NBTTagCompound;
import resonant.lib.collection.EvictingList;

/**
 * Graph for the multimeter
 *
 * @author Calclavia
 *
 */
public abstract class Graph<V extends Comparable<V>>
{
	public final String name;
	/**
	 * Each point represents a tick.
	 */
	protected final EvictingList<V> points;
	private final int maxPoints;
	/**
	 * Queue for the next update to insert into the graph.
	 */
	protected V queue = getDefault();
	private V peak = getDefault();

	public Graph(String name, int maxPoints)
	{
		this.name = name;
		this.maxPoints = maxPoints;
		points = new EvictingList<V>(maxPoints);
	}

	public void add(V y)
	{
		points.add(y);

		peak = getDefault();

		for (V point : points)
			if (point.compareTo(peak) > 0)
				peak = y;
	}

	public V getPeak()
	{
		return peak;
	}

	public V get(int x)
	{
		return points.size() > x ? points.get(x) : getDefault();
	}

	public V get()
	{
		return get(0);
	}

	public abstract void queue(V value);

	public void doneQueue()
	{
		add(queue);
	}

	public abstract V getDefault();

	public void load(NBTTagCompound nbt)
	{
		points.clear();
	}

	public abstract NBTTagCompound save();

	public abstract double getDouble();

	public abstract V getAverage();
}
