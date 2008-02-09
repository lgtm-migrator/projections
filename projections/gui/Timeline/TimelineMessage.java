package projections.gui.Timeline;


public class TimelineMessage implements Comparable
{
	public long Time;
	public int Entry;
	public int MsgLen;
	public int EventID;
	public int SenderEventID;

	private int destPEs[];
	private int numPEs;

	public int srcPE;

	/** A messages sent from srcPE, with eventid EventID */

	/** Single message constructor */
	public TimelineMessage(int srcPE, int senderEventID, long t,int e,int mlen,int EventID) {
		this(srcPE, senderEventID, t, e, mlen, EventID, null);
	}

	/** Multicast Constructor */
	public TimelineMessage(int srcPE, int senderEventID, long t, int e, int mlen, int EventID, int destPEs[]) {
		this.SenderEventID=senderEventID;
		this.srcPE = srcPE;
		Time=t;
		Entry=e;
		MsgLen=mlen;
		this.EventID = EventID;
		if (destPEs != null) {
			this.numPEs = destPEs.length;
		} else {
			this.numPEs = 0;
		}
		this.destPEs = destPEs;
	}

	/** Broadcast Constructor */
	public TimelineMessage(int srcPE, int senderEventID, long t, int e, int mlen, int EventID, int numPEs) {
		this.SenderEventID=senderEventID;
		Time=t;
		this.srcPE = srcPE;
		Entry=e;
		MsgLen=mlen;
		this.EventID = EventID;
		this.numPEs = numPEs;
		this.destPEs = null;
	}

	public int getSenderEventID() {
		return SenderEventID;
	}

	/** compare two timeline messages based on their source pe and their EventID */
	public int compareTo(Object o) {
		TimelineMessage other = (TimelineMessage)o;

		if(srcPE == other.srcPE){
			return EventID - other.EventID;

		} else {
			return srcPE-other.srcPE;
		}
	}
	
	public String destination(int totalPE){
		if(isMulticast()){
			String ds = "";
			for(int i=0;i<numPEs;i++){
				ds = ds + destPEs[i];
				if(i<numPEs-1)
					ds = ds + ",";
			}
			return "Multicast to " +numPEs + " PEs: " + ds;
		}
		else if(isBroadcast()) {

			if(numPEs == totalPE){
				return "Group Broadcast"; 
			} else {
				return "NodeGroup Broadcast"; 
			}
			
		} else {
			return "Unicast to unknown";
		}
				
	}

	public boolean isBroadcast(){
		return (numPEs>0 && destPEs == null);
	}

	public boolean isMulticast(){
		return (numPEs>0 && destPEs != null);
	}

	public boolean isUnicast(){
		return (numPEs==0);
	}
	
	
}
