package core.index.kdtree;

import core.index.MDIndex;
import core.index.key.MDIndexKey;
import core.utils.RangeUtils.SimpleDateRange.SimpleDate;
import core.utils.SchemaUtils.TYPE;

import java.util.List;

/**
 * Created by qui on 1/31/15.
 */
public class KDNode {

    private int dimension;
    private TYPE type;
    private Object value;
    private int numBuckets;

    private KDNode parent;
    private KDNode leftChild;
    private KDNode rightChild;

    public KDNode() {
        numBuckets = 1;
    }

    public int getParentDimension() {
        if (parent == null) {
            return -1;
        }
        return parent.dimension;
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    public void setValues(int dimension, TYPE type, MDIndexKey key) {
        this.dimension = dimension;
        this.type = type;
        this.value = getValue(dimension, type, key);
    }

    private Object getValue(int dimension, TYPE type, MDIndexKey key) {
        switch (type) {
            case INT:
                return key.getIntAttribute(dimension);
            case LONG:
                return key.getLongAttribute(dimension);
            case FLOAT:
                return key.getFloatAttribute(dimension);
            case DATE:
                return key.getDateAttribute(dimension);
            case STRING:
                return key.getStringAttribute(dimension, 20);
            default:
                throw new RuntimeException("Unknown dimension type: "+type);
        }
        // TODO(qui): deal with VARCHAR somewhere
    }

    private int compareKey(Object value, int dimension, TYPE type, MDIndexKey key) {
        switch (type) {
            case INT:
                return ((Integer) value).compareTo(key.getIntAttribute(dimension));
            case LONG:
                return ((Long) value).compareTo(key.getLongAttribute(dimension));
            case FLOAT:
                return ((Float) value).compareTo(key.getFloatAttribute(dimension));
            case DATE:
                return ((SimpleDate) value).compareTo(key.getDateAttribute(dimension));
            case STRING:
                return ((String) value).compareTo(key.getStringAttribute(dimension, 20));
            default:
                throw new RuntimeException("Unknown dimension type: "+type);
        }
    }

    private void incrementNumBuckets() {
        this.numBuckets++;
        if (this.parent != null) {
            this.parent.incrementNumBuckets();
        }
    }

    public KDNode insert(MDIndexKey key) {
        if (value == null) {
            return this;
        } else if (compareKey(value, dimension, type, key) > 0) {
            if (leftChild == null) {
                leftChild = new KDNode();
                leftChild.parent = this;
                if (rightChild == null) {
                    numBuckets = 2;
                    if (parent != null) { parent.incrementNumBuckets(); }
                }
                return leftChild;
            } else {
                return leftChild.insert(key);
            }
        } else {
            if (rightChild == null) {
                rightChild = new KDNode();
                rightChild.parent = this;
                if (leftChild == null) {
                    numBuckets = 2;
                    if (parent != null) { parent.incrementNumBuckets(); }
                }
                return rightChild;
            } else {
                return rightChild.insert(key);
            }
        }
    }

    public int getBucketId(MDIndexKey key, int start) {
        if (value == null) {
            return start;
        }
        if (compareKey(value, dimension, type, key) > 0) {
            if (leftChild == null) {
                return start;
            }
            return leftChild.getBucketId(key, start*2);
        }
        else {
            if (rightChild == null) {
                return start;
            }
            return rightChild.getBucketId(key, start*2+1);
        }
    }

    public List<MDIndex.Bucket> rangeSearch(MDIndexKey low, MDIndexKey high) {
        return null;
    }
}