import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

public class VelocityPublisher {
	
	
	private Publisher<geometry_msgs.Twist> publisher_turtle;
	private Publisher<geometry_msgs.Twist> publisher_husky;
	private Subscriber<nav_msgs.Odometry> subscriber_turtle;
	private Subscriber<nav_msgs.Odometry> subscriber_husky;
	
	private double deltaTime;
	private long oldTime;
	private boolean isTurtle;
	private boolean isHusky;
	
	private geometry_msgs.Twist twist;
	
	private static double linearMax=1;
	private static double angularMax=1;
	private static double linearMin=0.05;
	private static double angularMin=0.05;
	
	private double linear;
	private double angular; 
	
	private URG_Subscriber urg_Subscriber;

	public VelocityPublisher(ConnectedNode connectedNode) {
		urg_Subscriber=new URG_Subscriber(connectedNode);
		publisher_turtle = connectedNode.newPublisher("/cmd_vel_mux/input/teleop", geometry_msgs.Twist._TYPE);
		publisher_husky = connectedNode.newPublisher("/joy_teleop/cmd_vel", geometry_msgs.Twist._TYPE);
		subscriber_turtle=connectedNode.newSubscriber("/odom", nav_msgs.Odometry._TYPE);
		subscriber_turtle.addMessageListener(new MessageListener<nav_msgs.Odometry>() {
			@Override
			public void onNewMessage(nav_msgs.Odometry message) {
				isTurtle=true;
				twist=message.getTwist().getTwist();
			}
		});
		subscriber_husky=connectedNode.newSubscriber("/odometry/filtered", nav_msgs.Odometry._TYPE);
		subscriber_husky.addMessageListener(new MessageListener<nav_msgs.Odometry>() {
			@Override
			public void onNewMessage(nav_msgs.Odometry message) {
				isHusky=true;
				twist=message.getTwist().getTwist();
			}
		});
	}
	
	public void publishVelocity(double linear_Accel, double angular_Accel) {
		if(twist!=null) {
			if(isTurtle) {
				calcDeltaTime();
				geometry_msgs.Twist data=publisher_turtle.newMessage();
				double vl=linear+(linear_Accel*deltaTime)-(linear*deltaTime);
				double va=angular+(angular_Accel*deltaTime)-(angular*deltaTime);
				if(Math.abs(vl)<linearMin) {
					linear=0;
				}else {
					if(Math.abs(vl)>linearMax) {
						linear=vl>0 ? linearMax : -linearMax;
					}else {
						linear=vl;
					}
				}
				if(Math.abs(va)<angularMin) {
					angular=0;
				}else {
					if(Math.abs(va)>angularMax) {
						angular=va>0 ? angularMax : -angularMax;
					}else {
						angular=va;
					}
				}
				//System.out.println(String.format("%.3f", twist.getLinear().getX())+" : "+String.format("%.3f", twist.getAngular().getZ()));
				//System.out.println(String.format("%.3f", linear)+" : "+String.format("%.3f", angular));
				//System.out.println();
				data.getLinear().setX(linear);
				data.getAngular().setZ(angular);
				data=urg_Subscriber.limitTwist(data);
				System.out.println(String.format("%.3f", data.getLinear().getX())+" : "+String.format("%.3f", data.getAngular().getZ()));
				System.out.println();
				
				publisher_turtle.publish(data);
			}
			if(isHusky) {
				calcDeltaTime();
				geometry_msgs.Twist data=publisher_turtle.newMessage();
				double vl=linear+(linear_Accel*deltaTime)-(linear*deltaTime);
				double va=angular+(angular_Accel*deltaTime)-(angular*deltaTime);
				if(Math.abs(vl)<linearMin) {
					linear=0;
				}else {
					if(Math.abs(vl)>linearMax) {
						linear=vl>0 ? linearMax : -linearMax;
					}else {
						linear=vl;
					}
				}
				if(Math.abs(va)<angularMin) {
					angular=0;
				}else {
					if(Math.abs(va)>angularMax) {
						angular=va>0 ? angularMax : -angularMax;
					}else {
						angular=va;
					}
				}
				System.out.println(String.format("%.3f", twist.getLinear().getX())+" : "+String.format("%.3f", twist.getAngular().getZ()));
				System.out.println(String.format("%.3f", linear)+" : "+String.format("%.3f", angular));
				System.out.println();
				data.getLinear().setX(linear);
				data.getAngular().setZ(angular);
				publisher_husky.publish(data);
			}
		}
	}
	
	public double getLinear(){
		if(twist!=null) {
			return twist.getLinear().getX();
		}
		return 0;
	}
	
	public double getAngular(){
		if(twist!=null) {
			return twist.getAngular().getZ();
		}
		return 0;
	}
	
	private void calcDeltaTime() {
		long nowTime=System.nanoTime()/1000000;
		deltaTime=(double)(nowTime-oldTime)/1000;
		oldTime=nowTime;
	}
	
}