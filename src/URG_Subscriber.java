import java.util.ArrayList;
import java.util.List;

import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import javafx.scene.paint.Color;


public class URG_Subscriber {

	private sensor_msgs.LaserScan log;

	/************************************************************************************************************************/
	/**
	 * URGデータを処理する
	 * 
	 * @param viewer
	 * @param connectedNode
	 */
	public URG_Subscriber(ConnectedNode connectedNode) {
		Subscriber<sensor_msgs.LaserScan> subscriberURG=connectedNode.newSubscriber("/scan", sensor_msgs.LaserScan._TYPE);
		subscriberURG.addMessageListener(new MessageListener<sensor_msgs.LaserScan>() {
			@Override
			public void onNewMessage(sensor_msgs.LaserScan message) {
				log=message;
				float[] distances=message.getRanges();
				float[] xs=new float[distances.length];
				float[] ys=new float[distances.length];
				double m=message.getAngleIncrement();
				double angle=message.getAngleMin();
				for(int i=0;i<message.getRanges().length;i++) {
					float d=message.getRanges()[i];
					if(Float.isInfinite(d)||Float.isNaN(d)) {
						distances[i]=30;
					}
					xs[i]=(float)(d*Math.cos(angle));
					ys[i]=(float)(d*Math.sin(angle));
					angle+=m;
				}

				int index=0;
				double distance=Integer.MAX_VALUE;
				for(int i=0;i<message.getRanges().length;i++) {
					if(distances[i]<distance) {
						distance=distances[i];
						index=i;
					}
				}
			}
		});
	}

	public geometry_msgs.Twist limitTwist(geometry_msgs.Twist twist){
		if(log!=null) {
			float[] ranges=log.getRanges();
			int length=ranges.length;
			double linear=twist.getLinear().getX();
			double angleIncrement=log.getAngleIncrement();
			double limit=0.25;
			double limitWidth=0.25;
			if(linear!=0) {
				if(linear>0) {
					double min=Integer.MAX_VALUE;
					for(int i=0;i<length/4;i++) {
						int indexA=i;
						int indexB=length-1-i;
						double distanceA=Math.abs(ranges[indexA]*Math.cos(angleIncrement*indexA));
						double distanceB=Math.abs(ranges[indexB]*Math.cos(angleIncrement*indexB));
						double widthA=Math.abs(ranges[indexA]*Math.sin(angleIncrement*indexA));
						double widthB=Math.abs(ranges[indexB]*Math.sin(angleIncrement*indexB));
						min = widthA<limitWidth&&distanceA<min&&distanceA>limit ? distanceA : min;
						min = widthB<limitWidth&&distanceB<min&&distanceB>limit ? distanceB : min;
						System.out.println(ranges[indexA]+" : "+distanceA+" : "+distanceB+" : "+i+" : "+min);
					}
					if(min<0.4) {
						twist.getLinear().setX(0);
					}
				}else {
					double min=Integer.MAX_VALUE;
					for(int i=0;i<length/4;i++) {
						int indexA=length/2+i;
						int indexB=length/2-i;
						double distanceA=Math.abs(ranges[indexA]*Math.cos(angleIncrement*indexA));
						double distanceB=Math.abs(ranges[indexB]*Math.cos(angleIncrement*indexB));
						double widthA=Math.abs(ranges[indexA]*Math.sin(angleIncrement*indexA));
						double widthB=Math.abs(ranges[indexB]*Math.sin(angleIncrement*indexB));
						min = widthA<limitWidth&&distanceA<min&&distanceA>limit ? distanceA : min;
						min = widthB<limitWidth&&distanceB<min&&distanceB>limit ? distanceB : min;
					}
					if(min<0.4) {
						twist.getLinear().setX(0);
					}
				}
			}
		}else {
			twist.getLinear().setX(0);
			twist.getAngular().setZ(0);
		}
		return twist;
	}

}