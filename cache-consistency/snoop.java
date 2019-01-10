//package snoop;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class snoop {	
	/*****创建panel2~panel5******/
	static Mypanel panel2 =new Mypanel();
	static Mypanel panel3 =new Mypanel();
	static Mypanel panel4 =new Mypanel();
	static Mypanel panel5 =new Mypanel();

	/*********memory的标题*********/
	static String[] Mem_ca={
			"Memory","","","Memory","","","Memory","",""
	};

	/*********memory中的内容*********/
	static String[][] Mem_Content ={
			{"0","","","10","","","20","",""},{"1","","","11","","","21","",""},{"2","","","12","","","22","",""},
			{"3","","","13","","","23","",""},{"4","","","14","","","24","",""},{"5","","","15","","","25","",""},
			{"6","","","16","","","26","",""},{"7","","","17","","","27","",""},{"8","","","18","","","28","",""},
			{"9","","","19","","","29","",""}
	};
	
	static JComboBox<String> Mylistmodel1_1 = new JComboBox<>(new Mylistmodel());
	static class Mylistmodel extends AbstractListModel<String> implements ComboBoxModel<String>{		
		private static final long serialVersionUID = 1L;
		String selecteditem=null;
		private String[] test={"直接映射","两路组相联","四路组相联"};
		public String getElementAt(int index){
			return test[index];
		}
		public int getSize(){
			return test.length;
		}
		public void setSelectedItem(Object item){
			selecteditem=(String)item;
		}
		public Object getSelectedItem( ){
			return selecteditem;
		}
		public int getIndex() {
			for (int i = 0; i < test.length; i++) {
				if (test[i].equals(getSelectedItem()))
					return i;
			}
			return 0;
		}
	}

	static class Mylistmodel2 extends AbstractListModel<String> implements ComboBoxModel<String>{		
		private static final long serialVersionUID = 1L;
		String selecteditem=null;
		private String[] test={"读","写"};
		public String getElementAt(int index){
			return test[index];
		}
		public int getSize(){
			return test.length;
		}
		public void setSelectedItem(Object item){
			selecteditem=(String)item;
		}
		public Object getSelectedItem( ){
			return selecteditem;
		}
		public int getIndex() {
			for (int i = 0; i < test.length; i++) {
				if (test[i].equals(getSelectedItem()))
					return i;
			}
			return 0;
		}
		
	}

	static class Mypanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		JLabel label=new JLabel("访问");
		JLabel label_2=new JLabel("Process1");
		
		JTextField jtext=new JTextField("");
		JButton button=new JButton("单步");
		JButton nbutton=new JButton("连续");
		JComboBox<String> Mylistmodel = new JComboBox<>(new Mylistmodel2());
		
		
		/*********cache中的标题*********/
		String[] Cache_ca={"Cache","读/写","目标地址"};
		/*********cache中的内容*********/
		String[][] Cache_Content = {
				{"0"," "," "},{"1"," "," "},{"2"," "," "},{"3"," "," "}
		};
		/************cache的滚动模版***********/
		JTable table_1 = new JTable(Cache_Content,Cache_ca);
		JScrollPane scrollPane = new JScrollPane(table_1);

		/************memory的滚动模版**********
		JTable table_2 = new JTable(Mem_Content,Mem_ca); 
		JScrollPane scrollPane2 = new JScrollPane(table_2);
		*/
		public Mypanel(){
			super();
			setSize(350, 250);
			setLayout(null);
			
			/*****添加原件********/
			add(jtext);
			add(label);
			add(label_2);
			add(button);
			add(nbutton);
			add(Mylistmodel);
			add(scrollPane);
			//add(scrollPane2);
			
			/****设置原件大小与字体********/
			label_2.setFont(new Font("",1,16));
			label_2.setBounds(10, 10, 100, 30);
			
			label.setFont(new Font("",1,16));
			label.setBounds(10, 50, 40, 30);
			
			jtext.setFont(new Font("",1,15));
			jtext.setBounds(50, 50, 40, 30);
			
			Mylistmodel.setFont(new Font("",1,15));
			Mylistmodel.setBounds(95, 50, 50, 30);
			
			scrollPane.setFont(new Font("",1,15));
			scrollPane.setBounds(10, 90, 310, 90);
			
			//scrollPane2.setFont(new Font("",1,15));
			//scrollPane2.setBounds(10, 190, 310, 180);
			
			button.setFont(new Font("",1,15));
			button.setBounds(150,50, 80, 30);
			
			nbutton.setFont(new Font("",1,15));
			nbutton.setBounds(235,50, 80, 30);
			
			/******添加按钮事件********/
			button.addActionListener(this);
			nbutton.addActionListener(this);
		}
		
		public void init(){
			/******Mypanel的初始化******/
			//jtext.setText(" ");
			jtext.setEnabled(true);
    		Mylistmodel.setEnabled(true);
    		nbutton.setEnabled(true);
			Mylistmodel.setSelectedItem(null);
			for(int i=0;i<=3;i++)
				for(int j=1;j<=2;j++)
					Cache_Content[i][j]=" ";
			for(int i=0;i<=9;i++)
				for(int j=1;j<=2;j++)
					Mem_Content[i][j]=" ";
			setVisible(false);
			setVisible(true);
            for(int i=0;i<4;i++){
                    cacheState[i]=0;//cachestate初始化,无效
            }
		}
        public int []cacheState=new int[4];//cache状态:0 I　１S　２E

		//busstate，false读缺失　true写缺失
		public void bussnoop(Mypanel process,String address,boolean busEvent){
			
			int addnum = Integer.parseInt(address);
			
		        int select=4;
                for(int i=0;i<4;i++){
                    if(process.Cache_Content[i][2].equals(address)) {
                        select=i;break;
                    }
                }
                if(select!=4){
                    if(process.cacheState[select]==1){		//S
                        if(busEvent){		//写缺失
                            process.cacheState[select]=0;
                            process.Cache_Content[select][1]=" ";
                            process.Cache_Content[select][2]=" ";

                            System.out.printf("  %d status: invalid\n",addnum);
                        }
                    }
                    if(process.cacheState[select]==2){		//E
                        if(busEvent){		//写缺失
                            process.cacheState[select]=0;
                            process.Cache_Content[select][1]=" ";
                            process.Cache_Content[select][2]=" ";

                            System.out.printf("  %d write back, status: invalid\n",addnum);
                        }
                        else {		//读缺失
                            process.cacheState[select]=1;
                            process.Cache_Content[select][1]="写回";
                            process.Cache_Content[select][2]=address;
                            
                            System.out.printf("  %d write back, status: shared\n",addnum);
                        }
                    }

                }
		}
		
		int tag = 0;
		
        public void exProcess(Mypanel process){
            int Method=Mylistmodel1_1.getSelectedIndex();
            int WR=process.Mylistmodel.getSelectedIndex();
            String address=process.jtext.getText();
            if(WR==-1||address==null||Method==-1){
                System.out.println("error, please select wr or fill in address or method");
            }
            else {
            	
            	if(tag == 0) {
            		System.out.println("==========");
            		if(WR == 0)	System.out.printf("* read\n");
            		else if(WR == 1)	System.out.printf("* write\n");
            		tag++;
            		
            		jtext.setEnabled(false);
            		Mylistmodel.setEnabled(false);
            		nbutton.setEnabled(false);
            		return;
            	}
            	
                int select = 4;
                boolean hit = false;
                int addnum = Integer.parseInt(address);
                    switch (Method) {
                        case 0:		//direct
                            select = addnum % 4;
                            if (process.Cache_Content[select][2].equals(address)) {
                                hit = true;
                            }
                            else{
                                if(process.cacheState[select]==2){
                                    //写回
                                	
                                	if(tag == 3)	System.out.printf("  %d write back\n",Integer.parseInt(process.Cache_Content[select][2]));

                                    // Mem_Content[addnum][2]="process "+Integer.toString(processId)+" 写回";

                                }
                            }
                            break;
                        case 1:		//2-way
                            select = addnum % 2;
                            if (process.Cache_Content[select * 2][2].equals(address)) {		//hit
                                select = select * 2;
                                hit = true;
                            } else if (process.Cache_Content[select * 2 + 1][2].equals(address)) {
                                select = select * 2 + 1;
                                hit = true;
                            }
                            if(hit==false) {		//miss
                                if(cacheState[select*2]==0){		//选择无效的块
                                    select=select*2;
                                }else if(cacheState[select*2+1]==0){
                                    select=select*2+1;
                                }
                                else{
                                    int tmp=(int)(Math.random()*10)%2;
                                    select=select*2+tmp;
                                    if(process.cacheState[select]==2){		//E
                                        //写回
                                    }
                                }
                            }
                            break;
                        case 2:		//4-way
                            for (int i = 0; i < 4; i++) {
                                if (process.Cache_Content[i][2].equals(address)) {		//hit
                                    select = i;
                                    hit = true;
                                    break;
                                }
                            }
                            if(hit==false){		//miss
                                select=4;

                                for (int i = 0; i < 4; i++) {
                                    if(process.cacheState[i]==0){
                                        select=i;		//选择无效的块
                                        break;
                                    }
                                }
                                if(select==4){		//随机选择一块
                                    select=(int)(Math.random()*10)%4;
                                    if(process.cacheState[select]==2){		//E
                                        //写回
                                    }
                                }
                            }
                            break;
                        default:
                            System.out.println("无相联情况！");
                    }
                    
                    if(tag == 1) {
                		if(WR == 0)	System.out.printf("* read");
                		else if(WR == 1)	System.out.printf("* write");
                		if(hit)	System.out.println(" hit");
                		else	System.out.println(" miss");
                		tag++;
                		return;
                	}

                if(hit==true){		//hit
                    switch (WR){
                        case 0:		//read
                                process.Cache_Content[select][1]="读";
                                process.Cache_Content[select][2]=address;
                                break;
                        case 1:		//write
                            if( process.cacheState[select]==1){		//S
                                process.cacheState[select]=2;		//S -> E
                                process.Cache_Content[select][1]="写";
                                process.Cache_Content[select][2]=address;
                                //bus：invalid
                                if(process!=panel2){bussnoop(panel2,address,true);}
                                if(process!=panel3){bussnoop(panel3,address,true);}
                                if(process!=panel4){bussnoop(panel4,address,true);}
                                if(process!=panel5){bussnoop(panel5,address,true);}
                            }
                            else{		//E

                            }
                            break;
                    }
                    
                    if(tag == 2) {
                		if(WR == 0) {
                			System.out.printf("* read %d\n",addnum);
                			tag = 0;
                			jtext.setEnabled(true);
        	        		Mylistmodel.setEnabled(true);
        	        		nbutton.setEnabled(true);
                		}
                		else if(WR == 1 && process.cacheState[select] == 1) {
                			System.out.printf("* %d exclusive\n",addnum);
                			tag++;
                		}
                		else if(WR == 1 && process.cacheState[select] == 2) {
                			System.out.printf("* write %d\n",addnum);
                			tag = 0;
                			jtext.setEnabled(true);
        	        		Mylistmodel.setEnabled(true);
        	        		nbutton.setEnabled(true);
                		}
                		return;
                	}
                    if(tag == 3) {
                		if(WR == 1) {
                			System.out.printf("* write %d\n",addnum);
                			tag = 0;
                			jtext.setEnabled(true);
        	        		Mylistmodel.setEnabled(true);
        	        		nbutton.setEnabled(true);
                		}
                		return;
                	}
                }
                else{		//miss
                	if(tag == 2) {
                		if(WR == 0)	System.out.printf("* read miss, access mem\n");
                		else if(WR == 1)	System.out.printf("* write miss, access mem\n");
                		tag++;
                		return;
                	}
	                    if(tag == 3) {
	                		if(WR == 0)	System.out.printf("* get from mem\n");
	                		else if(WR == 1)	System.out.printf("* get from mem\n");
	                		tag++;
	                		return;
	                	}

	                    switch (WR){
	                        case 0:		//read
	                            process.cacheState[select]=1;
	                            //bus：read miss
	                            if(process!=panel2){bussnoop(panel2,address,false);}
	                            if(process!=panel3){bussnoop(panel3,address,false);}
	                            if(process!=panel4){bussnoop(panel4,address,false);}
	                            if(process!=panel5){bussnoop(panel5,address,false);}
	                            process.Cache_Content[select][1]="读";
	                            process.Cache_Content[select][2]=address;
	                            break;
	                        case 1:		//write
	                            process.cacheState[select]=2;		//E
	                            //bus：write miss
	                            if(process!=panel2){bussnoop(panel2,address,true);}
	                            if(process!=panel3){bussnoop(panel3,address,true);}
	                            if(process!=panel4){bussnoop(panel4,address,true);}
	                            if(process!=panel5){bussnoop(panel5,address,true);}
	
	                            process.Cache_Content[select][1]="写";
	                            process.Cache_Content[select][2]=address;
	                           break;
	                    }
	                    
	                    
	                    if(tag == 4) {
	                		if(WR == 0)	System.out.printf("* read %d\n",addnum);
	                		else if(WR == 1)	System.out.printf("* write %d, status: exclusive\n",addnum);
	                		tag = 0;
	                		jtext.setEnabled(true);
	    	        		Mylistmodel.setEnabled(true);
	    	        		nbutton.setEnabled(true);
	                		return;
	                	}
                    }
                }
            
	            
            }

		public void actionPerformed(ActionEvent e){
			/******编写自己的处理函数*******/
				if (e.getSource() == this.button) {
						exProcess(this);
				}
				else if(e.getSource() == this.nbutton) {
						do {
							exProcess(this);
						}while(tag != 0);
				}
				/**********显示刷新后的数据********/
				panel2.setVisible(false);
				panel2.setVisible(true);
				panel3.setVisible(false);
				panel3.setVisible(true);
				panel4.setVisible(false);
				panel4.setVisible(true);
				panel5.setVisible(false);
				panel5.setVisible(true);

			}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame myjf = new JFrame("多cache一致性模拟之监听法");
		myjf.setSize(1500, 600);
		myjf.setLayout(null);
		myjf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container C1 = myjf.getContentPane();

        JTable table_2 = new JTable(Mem_Content,Mem_ca);
        JScrollPane scrollPane2 = new JScrollPane(table_2);

		/*****新建panel1*****/
		JPanel panel1 = new JPanel();

		C1.add(panel2);
		C1.add(panel3);
		C1.add(panel4);
		C1.add(panel5);
		C1.add(scrollPane2);
		panel2.setBounds(10, 100, 350, 200);
		panel3.setBounds(350, 100, 350, 200);
		panel4.setBounds(700, 100, 350, 200);
		panel5.setBounds(1050, 100, 350, 200);
		scrollPane2.setBounds(200,350,1000,180);
		scrollPane2.setFont(new Font("",1,15));
		//scrollPane2.setBounds(100, 250, 310, 180);
		
		/********设置每个Mypanel的不同的参数************/
		panel2.label_2.setText("CPU1");
		panel3.label_2.setText("CPU2");
		panel4.label_2.setText("CPU3");
		panel5.label_2.setText("CPU4");
		panel2.table_1.getColumnModel().getColumn(0).setHeaderValue("cache1");
		panel2.Cache_ca[0]="Cache1";
		panel3.table_1.getColumnModel().getColumn(0).setHeaderValue("cache2");
		panel3.Cache_ca[0]="Cache2";
		panel4.table_1.getColumnModel().getColumn(0).setHeaderValue("cache3");
		panel4.Cache_ca[0]="Cache3";
		panel5.table_1.getColumnModel().getColumn(0).setHeaderValue("cache4");
		panel5.Cache_ca[0]="Cache4";
		
		
		//panel2.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory1");
		//panel3.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory2");
		//panel4.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory3");
		//panel5.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory4");
		
		for(int i=0;i<10;i++){
			//panel3.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel3.Mem_Content[i][0])+10));
			//panel4.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel3.Mem_Content[i][0])+20));
			//panel5.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel3.Mem_Content[i][0])+30));
		}
		/********设置头部panel*****/
		panel1.setBounds(10, 10, 1500, 100);
		panel1.setLayout(null);
		
		JLabel label1_1=new JLabel("执行方式:单步执行");
		label1_1.setFont(new Font("",1,20));
		label1_1.setBounds(15, 15, 200, 40);
		panel1.add(label1_1);
		
		//JComboBox<String> Mylistmodel1_1 = new JComboBox<>(new Mylistmodel());
		Mylistmodel1_1.setBounds(220, 15, 150, 40);
		Mylistmodel1_1.setFont(new Font("",1,20));
		panel1.add(Mylistmodel1_1);
		
		JButton button1_1=new JButton("复位");
		button1_1.setBounds(400, 15, 70, 40);
		
		/**********复位按钮事件（初始化）***********/
		button1_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				panel2.init();
				panel3.init();
				panel4.init();
				panel5.init();
				Mylistmodel1_1.setSelectedItem(null);

			}
		});
		
		/*panel2.Mem_Content[1][1]="11";*/
		panel1.add(button1_1);
		C1.add(panel1);
		myjf.setVisible(true);

	}
}
