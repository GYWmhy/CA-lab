import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.omg.CORBA.PUBLIC_MEMBER;

import jdk.internal.dynalink.beans.StaticClass;


class Instruction{
	String name;		//指令名称
	String opr1;		//目的操作数
	String opr2;		//源操作数1
	String opr3;		//源操作数2
}

class InstructionStation{
	String Qi;		//当前指令所在保留站名称
	int state;		//当前指令所处的状态
	int executetime;		//指令执行所需要的时间
	Instruction instruction;		//该指令状态所对应的指令
}

class ReservationStation{
	String Qi;		//Qi为该保留站的名称，以告知指定寄存器结果来源
	String Vj;		//指令第一个操作数：op2
	String Vk;		//指令第二个操作数：op3
	String Qj;		//产生指令第一个操作数值的保留站
	String Qk;		//产生指令第二个操作数值的保留站
	String Op;		//操作的类型
	String Busy;		//工作状态
}

class RegisterStation{
	String Qi;		//以该寄存器为目的寄存器的保留站名称 
	String value;		//该寄存器的值
	String state;		//寄存器此时的状态
}

class LoadStation{
	String Qi;		//保留站名称
	String Busy;		//Load组件状态
	String Addr;		//Load组件访存地址
	String value;		//访存值
}

public class Tomasulo extends JFrame implements ActionListener{
	/*
	 * 界面上有六个面板：
	 * ins_set_panel : 指令设置
	 * EX_time_set_panel : 执行时间设置
	 * ins_state_panel : 指令状态
	 * RS_panel : 保留站状态
	 * Load_panel : Load部件
	 * Registers_state_panel : 寄存器状态
	 */
	private JPanel ins_set_panel,EX_time_set_panel,ins_state_panel,RS_panel,Load_panel,Registers_state_panel;

	/*
	 * 四个操作按钮：步进，进5步，重置，执行
	 */
	private JButton stepbut,step5but,resetbut,startbut;

	/*
	 * 指令选择框
	 */
	private JComboBox inst_typebox[]=new JComboBox[24];

	/*
	 * 每个面板的名称
	 */
	private JLabel instl, timel, tl1,tl2,tl3,tl4,resl,regl,ldl,insl,stepsl;
	private int time[]=new int[4];

	/*
	 * 部件执行时间的输入框
	 */
	private JTextField tt1,tt2,tt3,tt4;

	private int intv[][]=new int[6][4],instnow=0;		//

	int cnow;		//
	private int cal[][]={{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0}};
	private int ld[][]={{0,0},{0,0},{0,0}};
	private int ff[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

	/*
	 * (1)说明：根据你的设计完善指令设置中的下拉框内容
	 * inst： 指令下拉框内容:"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"…………
	 * regist_table：       目的寄存器下拉框内容:"F0","F2","F4","F6","F8" …………
	 * rx：       源操作数寄存器内容:"R0","R1","R2","R3","R4","R5","R6","R7","R8","R9" …………
	 * ix：       立即数下拉框内容:"0","1","2","3","4","5","6","7","8","9" …………
	 */
	public static int m=0;
	private String  inst[]={"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"},
					regist_table[]={"F0","F2","F4","F6","F8","F10","F12","F14","F16","F18","F20","F22","F24","F26","F28","F30"},
					rx[]={"R0","R1","R2","R3","R4","R5","R6"},
					ix[]={"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25"};

	/*
	 * (2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
	 * 		指令状态 面板
	 * 		保留站 面板
	 * 		Load部件 面板
	 * 		寄存器 面板
	 * 					的大小
	 */
	/*my_inst_type：指令状态列表(7行4列) 
	 *my_rs：保留站列表(6行8列) 
	 *my_load：load缓存列表(4行4列) 
	 *my_regsters：寄存器列表(3行17列) 
	 * */
	private	String  my_inst_type[][]=new String[7][4], my_rs[][]=new String[6][8],
					my_load[][]=new String[4][4], my_regsters[][]=new String[3][17];
	private String  culinstst[][]=new String[7][4], culresst[][]=new String[6][8],		//
			        culldst[][]=new String[4][4], culregst[][]=new String[3][17];		//
	/*将以上String值加入到列表框中*/
	private	JLabel  inst_typejl[][]=new JLabel[7][4], resjl[][]=new JLabel[6][8],
					ldjl[][]=new JLabel[4][4], regjl[][]=new JLabel[3][17];
	
	/**
	 * 初始化指令队列，指令状态集，保留站，Load缓存站，寄存器站
	 */
	private Instruction instruction[] = new Instruction[6];
	private InstructionStation instructionstation[] = new InstructionStation[6];
	private ReservationStation RS[] = new ReservationStation[5];
	private LoadStation loadstation[] = new LoadStation[3];
	private RegisterStation RegS[] = new RegisterStation[16];
	

//构造方法
	public Tomasulo(){
		super("Tomasulo Simulator");

		//设置布局
		Container cp=getContentPane();
		FlowLayout layout=new FlowLayout();
		cp.setLayout(layout);

		//指令设置。GridLayout(int 指令条数, int 操作码+操作数, int hgap, int vgap)
		instl = new JLabel("指令设置");
		ins_set_panel = new JPanel(new GridLayout(6,4,0,0));
		ins_set_panel.setPreferredSize(new Dimension(350, 150));
		ins_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//操作按钮:执行，重设，步进，步进5步
		timel = new JLabel("执行时间设置");
		EX_time_set_panel = new JPanel(new GridLayout(2,4,0,0));
		EX_time_set_panel.setPreferredSize(new Dimension(280, 80));
		EX_time_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//指令状态
		insl = new JLabel("指令状态");
		ins_state_panel = new JPanel(new GridLayout(7,4,0,0));
		ins_state_panel.setPreferredSize(new Dimension(420, 175));
		ins_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//保留站
		resl = new JLabel("保留站");
		RS_panel = new JPanel(new GridLayout(6,7,0,0));
		RS_panel.setPreferredSize(new Dimension(420, 150));
		RS_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//Load部件
		ldl = new JLabel("Load部件");
		Load_panel = new JPanel(new GridLayout(4,4,0,0));
		Load_panel.setPreferredSize(new Dimension(200, 100));
		Load_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//寄存器状态
		regl = new JLabel("寄存器");
		Registers_state_panel = new JPanel(new GridLayout(3,17,0,0));
		Registers_state_panel.setPreferredSize(new Dimension(740, 75));
		Registers_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		tl1 = new JLabel("Load");
		tl2 = new JLabel("加/减");
		tl3 = new JLabel("乘法");
		tl4 = new JLabel("除法");

//操作按钮:执行，重设，步进，步进5步
		stepsl = new JLabel();
		stepsl.setPreferredSize(new Dimension(200, 30));
		stepsl.setHorizontalAlignment(SwingConstants.CENTER);
		stepsl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		stepbut = new JButton("步进");
		stepbut.addActionListener(this);
		step5but = new JButton("步进5步");
		step5but.addActionListener(this);
		startbut = new JButton("执行");
		startbut.addActionListener(this);
		resetbut= new JButton("重设");
		resetbut.addActionListener(this);
		/*设置执行周期初始值*/
		tt1 = new JTextField("2");
		tt2 = new JTextField("2");
		tt3 = new JTextField("10");
		tt4 = new JTextField("40");

//指令设置
		/*
		 * 设置指令选择框（操作码，操作数，立即数等）的default选择
		 */
		for (int i=0;i<2;i++)
			for (int j=0;j<4;j++){
				if (j==0){
					inst_typebox[i*4+j]=new JComboBox(inst);
				}
				else if (j==1){
					inst_typebox[i*4+j]=new JComboBox(regist_table);
				}
				else if (j==2){
					inst_typebox[i*4+j]=new JComboBox(ix);
				}
				else {
					inst_typebox[i*4+j]=new JComboBox(rx);
				}
				inst_typebox[i*4+j].addActionListener(this);
				ins_set_panel.add(inst_typebox[i*4+j]);
			}
		for (int i=2;i<6;i++)
			for (int j=0;j<4;j++){
				if (j==0){
					inst_typebox[i*4+j]=new JComboBox(inst);
				}
				else {
					inst_typebox[i*4+j]=new JComboBox(regist_table);
				}
				inst_typebox[i*4+j].addActionListener(this);
				ins_set_panel.add(inst_typebox[i*4+j]);
			}
		/*
		 * (3)说明：设置界面默认指令，根据你设计的指令，操作数等的选择范围进行设置。
		 * 默认6条指令。待修改
		 */
		/*L.D F6,21(R2)*/
		inst_typebox[0].setSelectedIndex(1);
		inst_typebox[1].setSelectedIndex(3);
		inst_typebox[2].setSelectedIndex(21);
		inst_typebox[3].setSelectedIndex(2);
        /*L.D F2,20(R3)*/
		inst_typebox[4].setSelectedIndex(1);
		inst_typebox[5].setSelectedIndex(1);
		inst_typebox[6].setSelectedIndex(20);
		inst_typebox[7].setSelectedIndex(3);
        /*MUL.D F0,F2,F4*/
		inst_typebox[8].setSelectedIndex(4);
		inst_typebox[9].setSelectedIndex(0);
		inst_typebox[10].setSelectedIndex(1);
		inst_typebox[11].setSelectedIndex(2);
        /*SUB.D F8,F6,F2*/
		inst_typebox[12].setSelectedIndex(3);
		inst_typebox[13].setSelectedIndex(4);
		inst_typebox[14].setSelectedIndex(3);
		inst_typebox[15].setSelectedIndex(1);
        /*DIV.D F10,F0,F6*/
		inst_typebox[16].setSelectedIndex(5);
		inst_typebox[17].setSelectedIndex(5);
		inst_typebox[18].setSelectedIndex(0);
		inst_typebox[19].setSelectedIndex(3);
        /*ADD.D F6,F8,F2*/
		inst_typebox[20].setSelectedIndex(2);
		inst_typebox[21].setSelectedIndex(3);
		inst_typebox[22].setSelectedIndex(4);
		inst_typebox[23].setSelectedIndex(1);

//执行时间设置
		EX_time_set_panel.add(tl1);
		EX_time_set_panel.add(tt1);
		EX_time_set_panel.add(tl2);
		EX_time_set_panel.add(tt2);
		EX_time_set_panel.add(tl3);
		EX_time_set_panel.add(tt3);
		EX_time_set_panel.add(tl4);
		EX_time_set_panel.add(tt4);

//指令状态设置
		for (int i=0;i<7;i++)
		{
			for (int j=0;j<4;j++){
				inst_typejl[i][j]=new JLabel(my_inst_type[i][j]);
				inst_typejl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				ins_state_panel.add(inst_typejl[i][j]);
			}
		}
//保留站设置
		for (int i=0;i<6;i++)
		{
			for (int j=0;j<8;j++){
				resjl[i][j]=new JLabel(my_rs[i][j]);
				resjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				RS_panel.add(resjl[i][j]);
			}
		}
//Load部件设置
		for (int i=0;i<4;i++)
		{
			for (int j=0;j<4;j++){
				ldjl[i][j]=new JLabel(my_load[i][j]);
				ldjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				Load_panel.add(ldjl[i][j]);
			}
		}
//寄存器设置
		for (int i=0;i<3;i++)
		{
			for (int j=0;j<17;j++){
				regjl[i][j]=new JLabel(my_regsters[i][j]);
				regjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				Registers_state_panel.add(regjl[i][j]);
			}
		}

//向容器添加以上部件
		cp.add(instl);
		cp.add(ins_set_panel);
		cp.add(timel);
		cp.add(EX_time_set_panel);

		cp.add(startbut);
		cp.add(resetbut);
		cp.add(stepbut);
		cp.add(step5but);

		cp.add(stepsl);
		cp.add(ins_state_panel);
		cp.add(insl);
		cp.add(RS_panel);
		cp.add(resl);
		cp.add(Load_panel);
		cp.add(ldl);
		cp.add(Registers_state_panel);
		cp.add(regl);

		stepbut.setEnabled(false);
		step5but.setEnabled(false);
		ins_state_panel.setVisible(false);
		insl.setVisible(false);
		RS_panel.setVisible(false);
		ldl.setVisible(false);
		Load_panel.setVisible(false);
		resl.setVisible(false);
		stepsl.setVisible(false);
		Registers_state_panel.setVisible(false);
		regl.setVisible(false);
		setSize(820,620);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

/*
 * 点击”执行“按钮后，根据选择的指令，初始化其他几个面板
 */
	public void init(){
		// get value
		for (int i=0;i<6;i++){
			intv[i][0]=inst_typebox[i*4].getSelectedIndex();
			if (intv[i][0]!=0){
				intv[i][1]=2*inst_typebox[i*4+1].getSelectedIndex();
				if (intv[i][0]==1){
					intv[i][2]=inst_typebox[i*4+2].getSelectedIndex();
					intv[i][3]=inst_typebox[i*4+3].getSelectedIndex();
				}
				else {
					intv[i][2]=2*inst_typebox[i*4+2].getSelectedIndex();
					intv[i][3]=2*inst_typebox[i*4+3].getSelectedIndex();
				}
			}
		}
		time[0]=Integer.parseInt(tt1.getText());
		time[1]=Integer.parseInt(tt2.getText());
		time[2]=Integer.parseInt(tt3.getText());
		time[3]=Integer.parseInt(tt4.getText());
		//System.out.println(time[0]);
		// set 0
		my_inst_type[0][0]="指令";
		my_inst_type[0][1]="流入";
		my_inst_type[0][2]="执行";
		my_inst_type[0][3]="写回";


		my_load[0][0]="名称";
		my_load[0][1]="Busy";
		my_load[0][2]="地址";
		my_load[0][3]="值";
		my_load[1][0]="Load1";
		my_load[2][0]="Load2";
		my_load[3][0]="Load3";
		my_load[1][1]="no";
		my_load[2][1]="no";
		my_load[3][1]="no";

		my_rs[0][0]="Time";
		my_rs[0][1]="名称";
		my_rs[0][2]="Busy";
		my_rs[0][3]="Op";
		my_rs[0][4]="Vj";
		my_rs[0][5]="Vk";
		my_rs[0][6]="Qj";
		my_rs[0][7]="Qk";
		my_rs[1][1]="Add1";
		my_rs[2][1]="Add2";
		my_rs[3][1]="Add3";
		my_rs[4][1]="Mult1";
		my_rs[5][1]="Mult2";
		my_rs[1][2]="no";
		my_rs[2][2]="no";
		my_rs[3][2]="no";
		my_rs[4][2]="no";
		my_rs[5][2]="no";

		my_regsters[0][0]="字段";
		for (int i=1;i<17;i++){
			//System.out.print(i+" "+regist_table[i-1];
			my_regsters[0][i]=regist_table[i-1];

		}
		my_regsters[1][0]="状态";
		my_regsters[2][0]="值";

	for (int i=1;i<7;i++)
	{
		instruction[i-1] = new Instruction();
		for (int j=0;j<4;j++){
			if (j==0){
				int temp=i-1;
				
				String op_type;		//op_type为指令类型
				op_type = inst[inst_typebox[temp*4].getSelectedIndex()]+" ";
				if (inst_typebox[temp*4].getSelectedIndex()==0) {		//NOP
					op_type=op_type;
					instruction[i-1].name=inst[inst_typebox[temp*4].getSelectedIndex()];
					instruction[i-1].opr1=regist_table[inst_typebox[temp*4+1].getSelectedIndex()];
					instruction[i-1].opr2=regist_table[inst_typebox[temp*4+2].getSelectedIndex()];
					instruction[i-1].opr3=regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
				}
				else if (inst_typebox[temp*4].getSelectedIndex()==1){		//LOAD
					instruction[i-1].name=inst[inst_typebox[temp*4].getSelectedIndex()];
					instruction[i-1].opr1=regist_table[inst_typebox[temp*4+1].getSelectedIndex()];
					instruction[i-1].opr2=ix[inst_typebox[temp*4+2].getSelectedIndex()];
					instruction[i-1].opr3=rx[inst_typebox[temp*4+3].getSelectedIndex()];
				}
				else {		//其他运算指令
					op_type=op_type+regist_table[inst_typebox[temp*4+1].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+2].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
					instruction[i-1].name=inst[inst_typebox[temp*4].getSelectedIndex()];
					instruction[i-1].opr1=regist_table[inst_typebox[temp*4+1].getSelectedIndex()];
					instruction[i-1].opr2=regist_table[inst_typebox[temp*4+2].getSelectedIndex()];
					instruction[i-1].opr3=regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
				}
				my_inst_type[i][j]=op_type;
			}
			else my_inst_type[i][j]="";
		 }
		/**
		 * 指令状态列表初始化
		 */
		instructionstation[i-1]=new InstructionStation();
		instructionstation[i-1].instruction=instruction[i-1];
		instructionstation[i-1].state=0;
		instructionstation[i-1].executetime=getTimeForEX(instruction[i-1]);
	}
		for (int i=1;i<6;i++)
		for (int j=0;j<8;j++)if (j!=1&&j!=2){
			my_rs[i][j]="";
		}
		for (int i=1;i<4;i++)
		for (int j=2;j<4;j++){
			my_load[i][j]="";
		}
		for (int i=1;i<3;i++)
		for (int j=1;j<17;j++){
			my_regsters[i][j]="";
		}
		instnow=0;
		for (int i=0;i<5;i++){
			for (int j=1;j<3;j++) cal[i][j]=0;
			cal[i][0]=-1;
		}
		for (int i=0;i<3;i++)
			for (int j=0;j<2;j++) ld[i][j]=0;
		for (int i=0;i<17;i++) ff[i]=0;
	
	for(int i=0;i<5;i++)		//设置保留站初值
	{
		RS[i]=new ReservationStation();
		RS[i].Qi=my_rs[i+1][1];
		RS[i].Busy=my_rs[i+1][2];
		RS[i].Op=my_rs[i+1][3];
		RS[i].Vj=my_rs[i+1][4];
		RS[i].Vk=my_rs[i+1][5];
		RS[i].Qj=my_rs[i+1][6];
		RS[i].Qk=my_rs[i+1][7];
	}
	for(int i=0;i<16;i++)		//设置寄存器站初值
	{
		RegS[i]=new RegisterStation();
		RegS[i].state=my_regsters[0][i+1];
		RegS[i].Qi=my_regsters[1][i+1];
		RegS[i].value=my_regsters[2][i+1];
	}
	for(int i=0;i<3;i++)		//设置load部件初值
	{
		loadstation[i]=new LoadStation();
		loadstation[i].Qi=my_load[i+1][0];
		loadstation[i].Busy=my_load[i+1][1];
		loadstation[i].Addr=my_load[i+1][2];
		loadstation[i].value=my_load[i+1][3];
	}
	
}//init()
	
	public int getTimeForEX(Instruction instruction){		//指令的执行时间
		if(instruction.name=="L.D")
		{
			return Integer.parseInt(tt1.getText());
//			return 2;
		}
		else if(instruction.name=="ADD.D" || instruction.name=="SUB.D")
		{
			return Integer.parseInt(tt2.getText());
//			return 2;
		}
		else if(instruction.name=="MULT.D")
		{
			return Integer.parseInt(tt3.getText());
//			return 10;
		}
		else if(instruction.name=="DIV.D")
		{
			return Integer.parseInt(tt4.getText());
//			return 40;
		}
		else {
			return 0;
		}
		
	}	
/*
 * 点击操作按钮后，用于显示结果
 */
	public void display(){
		for (int i=0;i<7;i++)
			for (int j=0;j<4;j++){
				inst_typejl[i][j].setText(my_inst_type[i][j]);
			}
		for (int i=0;i<6;i++)
			for (int j=0;j<8;j++){
				resjl[i][j].setText(my_rs[i][j]);
			}
		for (int i=0;i<4;i++)
			for (int j=0;j<4;j++){
				ldjl[i][j].setText(my_load[i][j]);
			}
		for (int i=0;i<3;i++)
			for (int j=0;j<17;j++){
				regjl[i][j].setText(my_regsters[i][j]);
			}
		stepsl.setText("当前周期："+String.valueOf(cnow-1));
	}

	public void actionPerformed(ActionEvent e){
//点击“执行”按钮的监听器
		if (e.getSource()==startbut) {
			for (int i=0;i<24;i++) inst_typebox[i].setEnabled(false);
			tt1.setEnabled(false);tt2.setEnabled(false);
			tt3.setEnabled(false);tt4.setEnabled(false);
			stepbut.setEnabled(true);
			step5but.setEnabled(true);
			startbut.setEnabled(false);
			//根据指令设置的指令初始化其他的面板
			init();
			cnow=1;
			//展示其他面板
			display();
			ins_state_panel.setVisible(true);
			RS_panel.setVisible(true);
			Load_panel.setVisible(true);
			Registers_state_panel.setVisible(true);
			insl.setVisible(true);
			ldl.setVisible(true);
			resl.setVisible(true);
			stepsl.setVisible(true);
			regl.setVisible(true);
		}
//点击“重置”按钮的监听器
		if (e.getSource()==resetbut) {
			m=0;
			for (int i=0;i<24;i++) inst_typebox[i].setEnabled(true);
			tt1.setEnabled(true);tt2.setEnabled(true);
			tt3.setEnabled(true);tt4.setEnabled(true);
			stepbut.setEnabled(false);
			step5but.setEnabled(false);
			startbut.setEnabled(true);
			ins_state_panel.setVisible(false);
			insl.setVisible(false);
			RS_panel.setVisible(false);
			ldl.setVisible(false);
			Load_panel.setVisible(false);
			resl.setVisible(false);
			stepsl.setVisible(false);
			Registers_state_panel.setVisible(false);
			regl.setVisible(false);

			init();
			cnow=1;
		}
//点击“步进”按钮的监听器
		if (e.getSource()==stepbut) {
			core();
			cnow++;
			display();
		}
//点击“进5步”按钮的监听器
		if (e.getSource()==step5but) {
			for (int i=0;i<5;i++){
				core();
				cnow++;
			}
			display();
		}

		for (int i=0;i<24;i=i+4)
		{
			if (e.getSource()==inst_typebox[i]) {
				if (inst_typebox[i].getSelectedIndex()==1){
					inst_typebox[i+2].removeAllItems();
					for (int j=0;j<ix.length;j++) inst_typebox[i+2].addItem(ix[j]);
					inst_typebox[i+3].removeAllItems();
					for (int j=0;j<rx.length;j++) inst_typebox[i+3].addItem(rx[j]);
				}
				else {
					inst_typebox[i+2].removeAllItems();
					for (int j=0;j<regist_table.length;j++) inst_typebox[i+2].addItem(regist_table[j]);
					inst_typebox[i+3].removeAllItems();
					for (int j=0;j<regist_table.length;j++) inst_typebox[i+3].addItem(regist_table[j]);
				}
			}
		}
	}
	
	private int load_free(LoadStation loadstation[]){
		for(int i=0;i<loadstation.length;i++)
		{
			if(loadstation[i].Busy=="no")
			{
				return i;
			}
		}
		return -1;
	}

	private int RS_free(InstructionStation instructionstation,ReservationStation RS[])
	{		
		if(instructionstation.instruction.name=="ADD.D" || instructionstation.instruction.name=="SUB.D")
		{
		    for(int i=0;i<3;i++)
		    {
			    if(RS[i].Busy=="no")
			    {
				    return i;
			    }
		    }
		}
		else if(instructionstation.instruction.name=="MULT.D" || instructionstation.instruction.name=="DIV.D")
		{
		    for(int i=3;i<5;i++)
		    {
			    if(RS[i].Busy=="no")
			    {
				    return i;
			    }
		    }
		}
		return -1;
	}

	private int get_IS_no(InstructionStation instructionstation[]) {
		   for(int i=0;i<instructionstation.length;i++)
		   {
			   if(instructionstation[i].state==0 && instructionstation[i].Qi!="NOP")
			   {
				   return i;
			   }
		   }
		   return -1;
	}

	private int[] get_EX1_no(InstructionStation instructionstation[]) {
		int n=0;
		for(int i=0;i<instructionstation.length;i++)
		{
			if(instructionstation[i].state==1 && instructionstation[i].Qi!="NOP")
			{
				n++;
			}
		}
		int ex1_no[] = new int[n];
        for(int i=0;i<n;i++)
        {
        	ex1_no[i]=-1;
        }

        for(int i=0,j=0;i<instructionstation.length;i++)
		{
			if(instructionstation[i].state==1 && instructionstation[i].Qi!="NOP")
			{
				ex1_no[j]=i;
				j++;
			}
		}
        return ex1_no;
	}

	private int[] get_EX2_no(InstructionStation instructionstation[]) {
		int n=0;
		for(int i=0;i<instructionstation.length;i++)
		{
			if(instructionstation[i].state==2 && instructionstation[i].Qi!="NOP")
			{
				n++;
			}
		}
		int ex2_no[] = new int[n];
        for(int i=0;i<n;i++)
        {
        	ex2_no[i]=-1;
        }

        for(int i=0,j=0;i<instructionstation.length;i++)
		{
			if(instructionstation[i].state==2 && instructionstation[i].Qi!="NOP")
			{
				ex2_no[j]=i;
				j++;
			}
		}
        return ex2_no;
	}
	
	private int[] get_WB_no(InstructionStation instructionstation[]) {
		int n=0;
		for(int i=0;i<instructionstation.length;i++)
		{
			if(instructionstation[i].state==3 && instructionstation[i].Qi!="NOP")
			{
				n++;
			}
		}
		int wb_no[] = new int[n];
        for(int i=0;i<n;i++)
        {
        	wb_no[i]=-1;
        }

        for(int i=0,j=0;i<instructionstation.length;i++)
		{
			if(instructionstation[i].state==3 && instructionstation[i].Qi!="NOP")
			{
				wb_no[j]=i;
				j++;
			}
		}
        return wb_no;
	}
	
/*
 * (4)说明： Tomasulo算法实现
 */
	public void core()
	{
	    int issue_no,ex1_no[],ex2_no[],wb_no[];
	    issue_no=this.get_IS_no(instructionstation);
	    ex1_no=this.get_EX1_no(instructionstation);
	    ex2_no=this.get_EX2_no(instructionstation);
	    wb_no=this.get_WB_no(instructionstation);

	    if(issue_no!=-1)		//发射指令：state置1(执行条件为原值等于0，即指令队列中的等待状态)
	    {
	    	InstructionStation instrsn=instructionstation[issue_no];
	    	if(instrsn.instruction.name=="L.D")		//LOAD
	    	{
	    		int num_idld;
	    		num_idld=this.load_free(loadstation);
	    		if(num_idld!=-1)
	    		{
	    			instrsn.Qi=loadstation[num_idld].Qi;
	    			loadstation[num_idld].Busy="yes";
	    			my_load[num_idld+1][1]=loadstation[num_idld].Busy;
	    			loadstation[num_idld].value=instrsn.instruction.opr2;
	    			my_load[num_idld+1][3]=loadstation[num_idld].value;
	    		}
	    		
	    	}
	    	else {		//运算指令
	    		int num_idrs;
	    		num_idrs=this.RS_free(instructionstation[issue_no], RS);
	    		if(num_idrs!=-1)
	    		{
	    			instrsn.Qi=RS[num_idrs].Qi;
	    			RS[num_idrs].Busy="yes";
	    			my_rs[num_idrs+1][2]=RS[num_idrs].Busy;
	    			RS[num_idrs].Op=instrsn.instruction.name;
	    			my_rs[num_idrs+1][3]=RS[num_idrs].Op;

	    			boolean opj,opk;
		    		opj=false;
		    		opk=false;
	    			for(int i=0;i<issue_no;i++)		//循环查询已发射指令，如其有目的寄存器作为当前指令的源操作数来源时进行处理
	    			{
	    				String destination;
	    				destination=instructionstation[i].instruction.opr1;

	    				if(instrsn.instruction.opr2==destination)		//源操作第一寄存器：Qj,Vj
	    				{
	    					opj=true;
	    					for(int j=0;j<RegS.length;j++)
	    					{
	    						if(RegS[j].state==destination)
	    						{
	    							if(RegS[j].value=="")		//第一个操作数的值等保留站
	    							{
	    								RS[num_idrs].Qj=RegS[j].Qi;
	    								my_rs[num_idrs+1][6]=RS[num_idrs].Qj;
	    							}
	    							else {		//第一个操作数有值
										RS[num_idrs].Vj=RegS[j].value;
										my_rs[num_idrs+1][4]=RS[num_idrs].Vj;
									}
	    						}
	    					}
	    				}

	    				if(instrsn.instruction.opr3==destination)		//源操作第二寄存器：Qk,Vk
	    				{
	    					opk=true;
	    					for(int j=0;j<RegS.length;j++)
	    					{
	    						if(RegS[j].state==destination)
	    						{
	    							if(RegS[j].value=="")		//第二个操作数的值等保留站
	    							{
	    								RS[num_idrs].Qk=RegS[j].Qi;
	    								my_rs[num_idrs+1][7]=RS[num_idrs].Qk;
	    							}
	    							else {		//第二个操作数有值
										RS[num_idrs].Vk=RegS[j].value;
										my_rs[num_idrs+1][5]=RS[num_idrs].Vk;
									}
	    						}
	    					}
	    				}
	    			}

	    			if(!opj)		//无寄存器相关，直接对保留站操作数进行赋值
	    			{
	    				RS[num_idrs].Vj=instrsn.instruction.opr2;
	    				my_rs[num_idrs+1][4]="R["+RS[num_idrs].Vj+"]";
	    			}
	    			if(!opk)
	    			{
	    				RS[num_idrs].Vk=instrsn.instruction.opr3;
	    				my_rs[num_idrs+1][5]="R["+RS[num_idrs].Vk+"]";
	    			}
	    			
	    		}
	    	
			}

	    	String destination2,Qi;
	    	destination2=instrsn.instruction.opr1;
	    	Qi=instrsn.Qi;
	    	for(int i=0;i<this.RegS.length;i++)		//寄存器站对该发射指令做出响应
	    	{
	    		if(RegS[i].state==destination2)
	    		{
	    			RegS[i].Qi=Qi;
	    			my_regsters[1][i+1]=RegS[i].Qi;
	    			break;
	    		}
	    	}

	    	my_inst_type[issue_no+1][1]=String.valueOf(cnow);		//修改该指令状态
	    	instructionstation[issue_no].state=1;
	    }//if
	    
	    for(int i=0;i<ex1_no.length;i++)		//进入执行阶段：state = 1
	    {
	    	if(ex1_no[i]!=-1)
	    	{
	    		InstructionStation instrnsex1=instructionstation[ex1_no[i]];

	    		if(instrnsex1.instruction.name=="L.D")		//load，更新地址栏，更新指令状态集中的执行列表
	    		{
	    			for(int j=0;j<loadstation.length;j++)
	    			{
	    				if(loadstation[j].Qi==instrnsex1.Qi)
	    				{
	    					loadstation[j].Addr="R["+instrnsex1.instruction.opr3+"]"+instrnsex1.instruction.opr2;
	    					my_load[j+1][2]=loadstation[j].Addr;
	    					instrnsex1.executetime--;
	    					break;
	    				}
	    			}
	    			if(instrnsex1.executetime>0)
	    			{
	    				my_inst_type[ex1_no[i]+1][2]=String.valueOf(cnow)+"~";
	    				instructionstation[ex1_no[i]].state=2;
	    			}
	    			else if(instrnsex1.executetime==0)
	    			{
	    				my_inst_type[ex1_no[i]+1][2]=String.valueOf(cnow);
	    				instructionstation[ex1_no[i]].state=3;
	    			}
	    		}
	    		else {		//运算指令，更新指令集执行时间列表
	    			String Qi2= instrnsex1.Qi;
					for(int j=0;j<RS.length;j++)
					{
						if(RS[j].Qi==instrnsex1.Qi)
						{
							if(!RS[j].Vj.equals("") && !RS[j].Vk.equals(""))
							{
								instrnsex1.executetime--;
								my_rs[j+1][0]=String.valueOf(instrnsex1.executetime);
								
								if(instrnsex1.executetime>0)
								{
									my_inst_type[ex1_no[i]+1][2]=String.valueOf(cnow)+"~";
									instructionstation[ex1_no[i]].state=2;
									break;
								}
								else if(instrnsex1.executetime==0)
								{
									my_inst_type[ex1_no[i]+1][2]=String.valueOf(cnow);
									instructionstation[ex1_no[i]].state=3;
									break;
								}
							}
						}
					}
				}
	    	}
	    }

	    for(int i=0;i<ex2_no.length;i++)		//执行到结束阶段，即state = 2，更新指令执行时间数
	    {
	    	if(ex2_no[i]!=-1)
	    	{
	    		InstructionStation instrnsex2=instructionstation[ex2_no[i]];

	    		if(instrnsex2.instruction.name=="L.D")		//load，更新load缓存中的值
	    		{
	    			for(int j=0;j<loadstation.length;j++)
	    			{
	    				if(loadstation[j].Qi==instrnsex2.Qi)
	    				{
	    					loadstation[j].value="M["+loadstation[j].Addr+"]";
	    					my_load[j+1][3]=loadstation[j].value;
	    					instrnsex2.executetime--;
	    					break;
	    				}
	    			}
	    			if(instrnsex2.executetime==0)
	    			{
	    				my_inst_type[ex2_no[i]+1][2]+=String.valueOf(cnow);
	    				instructionstation[ex2_no[i]].state=3;
	    			}
	    		}
	    		else {		//运算指令,更新保留站中的执行计时时间
	    			int j;
	    			for(j=0;j<RS.length;j++)
					{
						if(RS[j].Qi==instrnsex2.Qi)
						{
						    instrnsex2.executetime--;
							my_rs[j+1][0]=String.valueOf(instrnsex2.executetime);
							break;
						}
					}
	    			if(instrnsex2.executetime==0)
					{
						my_inst_type[ex2_no[i]+1][2]+=String.valueOf(cnow);
						instructionstation[ex2_no[i]].state=3;
						my_rs[j+1][0]="";
					}
				}
	    	}
	    }
	    
	    for(int i=0;i<wb_no.length;i++)		//执行完毕，写回
	    {
	    	if(wb_no[i]!=-1)
	    	{
	    		InstructionStation instrnswb=instructionstation[wb_no[i]];
	    		String Qi4=instrnswb.Qi;
	    		if(instrnswb.instruction.name=="L.D")		//load，写回，取消对load缓存站的占用
	    		{
	    			for(int j=0;j<loadstation.length;j++)
	    			{
	    				if(loadstation[j].Qi==instrnswb.Qi)
	    				{
	    					loadstation[j].Busy="no";
	    					loadstation[j].Addr="";
	    					loadstation[j].value="";
	    					my_load[j+1][1]=loadstation[j].Busy;
	    					my_load[j+1][2]=loadstation[j].Addr;
	    					my_load[j+1][3]=loadstation[j].value;
	    					break;
	    				}
	    			}
	    		}
	    		else {		//其他运算指令时更新保留站信息，取消对相应站的占用
					for(int j=0;j<RS.length;j++)
					{
						if(RS[j].Qi==Qi4)
						{
							RS[j].Busy="no";
							RS[j].Op="";
							RS[j].Qj="";
							RS[j].Qk="";
							RS[j].Vj="";
							RS[j].Vk="";
							my_rs[j+1][2]=RS[j].Busy;
							for(int k=3;k<8;k++)
								my_rs[j+1][k]="";	
							break;
						}
					}
				}

	    		for(int j=0;j<RegS.length;j++)		//更新指令目的寄存器对应的寄存器站
	    		{
	    			if(RegS[j].Qi==Qi4)
	    			{
	    				m++;
	    				RegS[j].value="M"+m;
	    				my_regsters[2][j+1]=RegS[j].value;
	    			}
	    		}

	    		for(int j=0;j<RS.length;j++)		//更新保留站中需要该寄存器值的源操作数
	    		{
	    			if(RS[j].Qj==Qi4)
	    			{
	    				RS[j].Vj="M"+m;
	    				RS[j].Qj="";
	    				my_rs[j+1][4]=RS[j].Vj;
	    				my_rs[j+1][6]=RS[j].Qj;
	    				continue;
	    			}
	    			if(RS[j].Qk==Qi4)
	    			{
	    				RS[j].Vk="M"+m;
	    				RS[j].Qk="";
	    				my_rs[j+1][5]=RS[j].Vk;
	    				my_rs[j+1][7]=RS[j].Qk;
	    			}
	    		}
	    		my_inst_type[wb_no[i]+1][3]=String.valueOf(cnow);
	    		instructionstation[wb_no[i]].state=4;
	    	}
	    }
	    
		boolean completed=true;
		for(int l=0;l<instructionstation.length;l++)
		{
			if(instructionstation[l].instruction.name!="NOP" && my_inst_type[l+1][3]=="")
			{
				completed=false;
				break;
			}
		}
		if(completed==true)
		{
			stepbut.setEnabled(false);
			step5but.setEnabled(false);
		}
	   
	}

	public static void main(String[] args) {
		new Tomasulo();
	}
}
