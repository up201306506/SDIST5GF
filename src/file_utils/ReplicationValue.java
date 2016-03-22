package file_utils;

public class ReplicationValue {
	private int replicationDegree;
	private int replicationValue;
	
	public ReplicationValue(int replicationDegree, int replicationValue){
		this.replicationDegree = replicationDegree;
		this.replicationValue = replicationValue;
	}
	
	public boolean replicationValueAboveOrEqualToDegree(){
		return (replicationValue >= replicationDegree);
	}
	
	public void incrementReplicationValue(){
		replicationValue++;
	}
	
	public void decrementReplicationValue(){
		if(replicationValue > 0) replicationValue--;
	}
}