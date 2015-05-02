package core.access.spark;

import org.apache.hadoop.conf.Configuration;

import com.google.common.base.Joiner;

import core.access.Predicate;

public class SparkQueryConf {

	public final static String DATASET = "DATASET";
	public final static String PREDICATES = "PREDICATES";
	public final static String WORKERS = "WORKERS";
	public final static String MAX_SPLIT_SIZE = "MAX_SPLIT_SIZE";
	public final static String ZOOKEEPER_HOSTS = "ZOOKEEPER_HOSTS";
	public final static String HADOOP_HOME = "HADOOP_HOME";	// this can be obtained from cartilage properties file
	
	public final static String CARTILAGE_PROPERTIES = "CARTILAGE_PROPERTIES";

	private Configuration conf;

	public SparkQueryConf(Configuration conf){
		this.conf = conf;
	}

	public void setDataset(String dataset){
		conf.set(DATASET, dataset);
	}

	public String getDataset(){
		return conf.get(DATASET);
	}

	public void setPredicates(Predicate[] predicates){
		conf.set(PREDICATES, Joiner.on(",").join(predicates));
	}

	public Predicate[] getPredicates(){
		String[] tokens = conf.get(PREDICATES).split(",");
		Predicate[] predicates = new Predicate[tokens.length];
		for(int i=0; i<predicates.length; i++)
			predicates[i] = new Predicate(tokens[i]);
		return predicates;
	}

	public void setWorkers(int workers){
		conf.set(WORKERS, ""+workers);
	}

	public int getWorkers(){
		return Integer.parseInt(conf.get(WORKERS));
	}

	public void setMaxSplitSize(int maxSplitSize){
		conf.set(MAX_SPLIT_SIZE, ""+maxSplitSize);
	}

	public int getMaxSplitSize(){
		return Integer.parseInt(conf.get(MAX_SPLIT_SIZE));
	}

	public void setZookeeperHosts(String hosts){
		conf.set(ZOOKEEPER_HOSTS, ""+hosts);
	}

	public String getZookeeperHosts(){
		return conf.get(ZOOKEEPER_HOSTS);
	}

	public void setHadoopHome(String home) {
		conf.set(HADOOP_HOME, home);
	}

	public String getHadoopHome() {
		return conf.get(HADOOP_HOME);
	}
	
	public void setCartilageProperties(String cartilageProperties){
		conf.set(CARTILAGE_PROPERTIES, cartilageProperties);
	}
	
	public String getCartilageProperties(){
		return conf.get(CARTILAGE_PROPERTIES);
	}
}
