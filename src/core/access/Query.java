package core.access;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.hadoop.io.Text;

import com.google.common.base.Joiner;

import core.access.iterator.IteratorRecord;


public class Query implements Serializable{
	private static final long serialVersionUID = 1L;
	
	protected Predicate[] predicates;

	public Predicate[] getPredicates() {
		return this.predicates;
	}

	public void write(DataOutput out) throws IOException{
		Text.writeString(out, Joiner.on(",").join(predicates));
		//out.writeBytes(Joiner.on(",").join(predicates)+"\n");
	}

	public void readFields(DataInput in) throws IOException{
		String[] tokens = Text.readString(in).split(",");
		//String[] tokens = in.readLine().split(",");
		predicates = new Predicate[tokens.length];
		for(int i=0; i<predicates.length; i++)
			predicates[i] = new Predicate(tokens[i]);
	}
	


	public static class FilterQuery extends Query implements Serializable{
		private static final long serialVersionUID = 1L;
		
		public FilterQuery() {
			this.predicates = null;
		}

		public FilterQuery(String predString) {
			String[] parts = predString.split(";");
			this.predicates = new Predicate[parts.length];
			for (int i=0; i<parts.length; i++) {
				this.predicates[i] = new Predicate(parts[i]);
			}
		}

		public FilterQuery(Predicate[] predicates) {
			this.predicates = predicates;
		}

		public boolean qualifies(IteratorRecord record){
			boolean qualify = true;
			for (Predicate p: predicates) {
				int attrIdx = p.attribute;
				switch(record.types[attrIdx]){
				case BOOLEAN:	qualify &= p.isRelevant(record.getBooleanAttribute(attrIdx)); break;
				case INT:		qualify &= p.isRelevant(record.getIntAttribute(attrIdx)); break;
				case LONG:		qualify &= p.isRelevant(record.getLongAttribute(attrIdx)); break;
				case FLOAT:		qualify &= p.isRelevant(record.getFloatAttribute(attrIdx)); break;
				case DATE:		qualify &= p.isRelevant(record.getDateAttribute(attrIdx)); break;
				case STRING:	qualify &= p.isRelevant(record.getStringAttribute(attrIdx,20)); break;
				case VARCHAR:	qualify &= p.isRelevant(record.getStringAttribute(attrIdx,100)); break;
				default:		throw new RuntimeException("Invalid data type!");
				}
			}
			return qualify;
		}

		@Override
		public String toString() {
			return Joiner.on(";").join(predicates);	// simpler impl			
//			String ret = "";
//			for (int i=0; i<this.predicates.length; i++) {
//				ret += this.predicates[i].toString() + ";";
//			}
//			ret += "\n";
//			return ret;
		}
		
		public static FilterQuery read(DataInput in) throws IOException {
			FilterQuery q = new FilterQuery();
	        q.readFields(in);
	        return q;
		}
	}

	public class JoinQuery extends Query{
		private static final long serialVersionUID = 1L;
	}
}
