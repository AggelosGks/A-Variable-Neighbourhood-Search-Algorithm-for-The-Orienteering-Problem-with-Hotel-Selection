

	public  class NodeWithDist implements Comparable<NodeWithDist> {
		
		public final Node node;
		public final double dist;
		public final int score;
		
		
		public NodeWithDist(Node node, double dist,int score) {
			this.node = node;
			this.dist = dist;
			this.score=score;
		}
		public NodeWithDist(Node node){
			this.node=node;
			this.dist = 0;
			this.score=0;
		}
		
		
		public int compareTo(NodeWithDist other) {
			//check distances first
			if (dist < other.dist)
				return -1;
			else if (dist > other.dist)
				return 1;
			//if distances are the same break the tie with node with maximal score
			else if(score<other.score){
				return 1;
			}else if(score>other.score){
				return -1;
			}
			else
				return 0;
		}
		
	}