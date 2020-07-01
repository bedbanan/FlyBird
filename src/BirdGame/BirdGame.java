package BirdGame;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

class Ground{    //地面类
	BufferedImage image;
	int x;
	int y;
	int width;
	int height;
	public Ground() throws Exception{
		image=ImageIO.read(getClass().getResource("ground.png"));
		width=image.getWidth();
		height=image.getHeight();
		x=0;
		y=500;  //地表的起始位置
	}
	public void step() {   //地面移动
		x--;
		if(x==-109) {
			x=0;
		}
	}
}
class Column{   //柱子类
	BufferedImage image;
	int x;  
	int y;  //x，y为柱子中心点的坐标
	int width;
	int height;
	int gap;  //柱子中间的间距
	int distance; //两根柱子之间的距离
	Random random=new Random();  //实例化随机生成函数
	public Column(int n) throws Exception {     //n代表第n根柱子
		image=ImageIO.read(getClass().getResource("column.png"));
		width=image.getWidth();
		height=image.getHeight();
		gap=144;
		distance=245;
		x=550+(n-1)*distance;  //背景图的x轴长432+距离第一根柱子的距离118=550,在固定第一根和第二根之间的距离固定为245
		y=random.nextInt(218)+132;//设定柱子y的距离限定在132到350之间设定[0-218]之间的随机数，加上132等于[132,350]
	}
	public void step() {
		x--;     
		if(x==-width/2) {  //当柱子向后移动出界时，x坐标为x=distance*2-width/2 其中distance*2是因为又要重新入界
			x=distance*2-width/2;
			y=random.nextInt(218)+132;
		}
	}
}
class Bird{    //鸟类
	BufferedImage image;
	int x;
	int y;
	int width;
	int height;
	int size;  //鸟的尺寸
	int index;  //鸟飞行的帧数数组的下标
	BufferedImage[] images; //定义一个数组的图片，作为鸟的动画帧
	double g; //重力加速度
	double t; //两次位置间的间隔时间
	double v0;//初始上抛速度
	double speed; //当前上抛的速度
	double s; //经过t时间以后的位移
	double alpha;//鸟的倾角，弧度单位
	public Bird() throws Exception{
		image=ImageIO.read(getClass().getResource("0.png"));
		width=image.getWidth();
		height=image.getHeight();
		x=132;
		y=180;
		size=40;
		g=4;
		t=0.25;
		v0=20;
		speed=v0;
		alpha=0;
		images=new BufferedImage[8];
		for(int i=0;i<8;i++) {
			images[i]=ImageIO.read(getClass().getResource(i+".png"));
		}
		index=0;
	}
	public void fly() {  //飞行
		index++;
		image=images[(index/12)%8];  //index/12是为了放慢切换图片的速度，%8是为了取得8以内的数
		
	}
	public void flappy() {   //点击上扬
		speed=v0;  
	}
	public void step() {   //鸟的移动方法
		double v0=speed;
		s=v0*t+g*t*t/2;  //平抛运动y轴的位移公式
		y=y-(int)s;  //计算鸟的坐标y
		double v=v0-g*t;//计算下次的速度
		speed=v;
		alpha=Math.atan(s/8);  //假设位移固定数为8，倾角就=反正切/8 调用API的反正切函数计算
	}
	public boolean hit(Ground ground) {
		boolean hit=y+size/2>ground.y;  //当鸟的y坐标+size/2小于地面的y轴时，与地面碰撞
		if(hit) {    //如果碰撞
			y=ground.y-size/2;  //将鸟放置在地上
			alpha=-3.14159265358979323/2;  //使鸟碰撞地面的时候有摔倒的效果
		}
		return hit;
	}
	/**
	 * 当鸟与柱子碰撞时，他们的x轴横坐标 X1=column.x-width/2-size/2，x2=column.x+width/2+size/2  x1是从前面碰到柱子
	 * x2是从后面碰到
	 * 同理，如果想通过，鸟的y轴纵坐标应该为 y1=cloumn.y-gap/2+size/2  y2=column.y+gao/2-size/2 柱子的中心-空隙/2再加鸟的一半
	 * @param column
	 * @return
	 */
	public boolean hit(Column column) {  //鸟碰撞到柱子
		if(x>column.x-width/2-size/2&&x<column.x+width/2+size/2) {  //先检测是否在柱子范围内
			if(y>column.y-column.gap/2+size/2&&y<column.y+column.gap/2-size/2) {  //检测是否在缝隙中
				return false;
			}
			return true;
		}
		return false; 
	}
}
public class BirdGame extends JPanel{
	Bird bird;
	Column column1,column2;
	Ground ground;
	
	BufferedImage background;
	int score;  //分数
	//boolean gameOver;   //用于标识游戏是否结束，当为false是代表未结束
	int state;
	public static final int START=0;  //游戏开始
	public static final int RUNNING=1; //游戏运行
	public static final int GAME_OVER=2; //游戏结束
	BufferedImage gameOverImage;
	BufferedImage startImage;
	public BirdGame() throws Exception{
		state=START;
		//gameOver=false;  //默认为false 既未结束
		startImage=ImageIO.read(getClass().getResource("start.png"));
		gameOverImage =ImageIO.read(getClass().getResource("gameover.png"));
		bird = new Bird();
		column1 = new Column(1);
		column2 = new Column(2);
		ground = new Ground();
		background=ImageIO.read(getClass().getResource("bg.png"));
	}
	public void paint(Graphics g) {
		g.drawImage(background,0,0,null);  //绘制背景
		//绘制柱子的起点的x坐标等于柱子中心点减去柱子的1/2宽，同理高也一样
		g.drawImage(column1.image,column1.x-column1.width/2,column1.y-column1.height/2,null);
		g.drawImage(column2.image,column2.x-column2.width/2,column2.y-column2.height/2,null);
		g.drawImage(ground.image,ground.x,ground.y,null);//绘制地面
		Graphics2D g2=(Graphics2D) g;
		g2.rotate(-bird.alpha,bird.x,bird.y);  //rotate旋转绘图坐标系，是API方法
		g.drawImage(bird.image,bird.x-bird.width/2,bird.y-bird.height/2,null);  //绘制小鸟
		g2.rotate(bird.alpha,bird.x,bird.y);  
		Font f=new Font(Font.SANS_SERIF,Font.BOLD,40);   //绘制分数
		g.setFont(f);  //设置分数
		g.drawString(""+score,40,60);
		g.setColor(Color.WHITE);  
		g.drawString(""+score,40-3,60-3);
		/*if(gameOver) {   //判断该函数的如果为true就返回gameover界面
			g.drawImage(gameOverImage,0,0,null);
		}*/
		switch (state) {
		case START:
			g.drawImage(startImage,0,0,null);
			break;
		case GAME_OVER:
			g.drawImage(gameOverImage,0,0,null);
			break;
		}
	} 
	/**
	 * 利用死循环来达到移动
	 * 1.修改地面位置，调用Ground类中的step方法
	 * 2.重新绘制界面，调用JPanel的repaint方法
	 * 3.利用多线程的方法实现一秒三十次刷新率
	 * @throws Exception
	 */
	public void action()throws Exception{
		MouseListener l=new MouseAdapter() {         //匿名内部类：鼠标按下
			public void mousePressed(MouseEvent e) {
				//bird.flappy();  //鸟上扬
				try {
					switch (state) {
					case GAME_OVER:
						column1=new Column(1);
						column2=new Column(2);
                        bird=new Bird();
						score=0;
						state=START;
						break;
					case START:
						state=RUNNING;
					case RUNNING: 
						bird.flappy(); //鸟在上扬
					
					}
				}catch (Exception ex) {
			          ex.printStackTrace();// TODO: handle exception
				}
			}
		};
		addMouseListener(l);   //将其挂到当前的面板game上
		while(true) {
			/*if(!gameOver) {  //如果gameOver不是为true则执行以下操作
			ground.step();  
			column1.step();//第一根柱子的刷新
			column2.step();
			bird.step();
			bird.fly();
			}
			if(bird.hit(ground)||bird.hit(column1)||bird.hit(column2)) {
				gameOver=true;    //如果碰撞则gameOver为true  画面终止
			}
			if(bird.x==column1.x||bird.x==column2.x) {
				score++;
			}*/   //计分逻辑
			switch (state) {
			case START:
				bird.fly();
				ground.step();
				break;
			case RUNNING:
				ground.step();  //地面移动
				column1.step();//第一根柱子的刷新
				column2.step();
				bird.step();  //鸟移动
				bird.fly();  //鸟在飞
				if(bird.x==column1.x||bird.x==column2.x) {  //计分逻辑
					score++;
				}
				if(bird.hit(ground)||bird.hit(column1)||bird.hit(column2)) {
					state=GAME_OVER;    //如果碰撞则gameOver为true  画面终止
				}
				break;
			}
			repaint();
			Thread.sleep(1000/80);//每秒刷新60次	
		}
	}
    public static void main(String args[])throws Exception{
    	JFrame frame=new JFrame();
    	BirdGame game=new BirdGame();
    	frame.add(game);
    	frame.setSize(440,670);
    	frame.setLocationRelativeTo(null); //设置窗口的固定位置
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //点击右上角的X就会推出
    	frame.setVisible(true);  //初始化窗口
    	game.action();   //调用该方法实现移动和刷新
    }
    
}
