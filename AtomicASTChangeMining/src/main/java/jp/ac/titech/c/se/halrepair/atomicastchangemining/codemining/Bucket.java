package jp.ac.titech.c.se.halrepair.atomicastchangemining.codemining;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

public class Bucket {
	private final int hashcode;
	private Set<Fragment> fragments = new HashSet<>();

	public Bucket(int hashcode) {
		this.hashcode = hashcode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Bucket) {
			Bucket other = (Bucket) obj;
			return this.hashcode == other.hashcode;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.hashcode;
	}

	public Set<Fragment> getFragments(){
		return fragments;
	}

	@Override
	public String toString() {
		return "Bucket: " + hashcode + ", fragments = " + fragments;
	}

	void addFragment(Fragment fragment) {
		this.fragments.add(fragment);
		if (fragment.getBuckets() == null){
			fragment.setBuckets(new HashSet<Bucket>());
		}
		fragment.getBuckets().add(this);
	}

	void removeFragment(Fragment fragment) {
		fragment.getBuckets().remove(this);
		this.fragments.remove(fragment);
		if (fragment.getBuckets().isEmpty())
			fragment.setBuckets(null);
	}

	private boolean isDuplicate(Bucket b) {
		return b != null
				&& this != b
				&& fragments.size() == b.fragments.size()
				&& b.fragments.containsAll(fragments);
	}

	private boolean isCovered(Bucket other) {
		return other != null
				&& this != other
				&& other.fragments.containsAll(fragments);
	}

	boolean isCovered(Set<Bucket> buckets) {
		for (Bucket b : buckets){
			if (isCovered(b)) return true;
		}
		return false;
	}

	boolean isDuplicate(HashMap<Integer, Bucket> buckets) {
		for (Bucket b : buckets.values()){
			if (isDuplicate(b)) return true;
		}
		return false;
	}

	boolean isDuplicate(HashSet<Bucket> buckets) {
		for (Bucket b : buckets){
			if (isDuplicate(b)) return true;
		}
		return false;
	}

	public void clear() {
		for (Fragment f : fragments){
			f.getBuckets().remove(this);
		}
		fragments.clear();
		fragments = null;
	}
}
