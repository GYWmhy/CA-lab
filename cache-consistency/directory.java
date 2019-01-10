import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class directory {	
	/*****创建panel2~panel5******/
	static Mypanel panel2 =new Mypanel();
	static Mypanel panel3 =new Mypanel();
	static Mypanel panel4 =new Mypanel();
	static Mypanel panel5 =new Mypanel();
	
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
				{"0","",""},{"1","",""},{"2","",""},{"3","",""}
		};
		/*********memory的标题*********/
		String[] Mem_ca={
				"Memory","",""
		};
		
		/*********memory中的内容*********/
		String[][] Mem_Content ={
				{"0","",""},{"1","",""},{"2","",""},{"3","",""},{"4","",""},{"5","",""},{"6","",""},{"7","",""},
				{"8","",""},{"9","",""}
		};
		/************cache的滚动模版***********/
		JTable table_1 = new JTable(Cache_Content,Cache_ca); 
		JScrollPane scrollPane = new JScrollPane(table_1);
		/************memory的滚动模版***********/
		JTable table_2 = new JTable(Mem_Content,Mem_ca); 
		JScrollPane scrollPane2 = new JScrollPane(table_2);
		
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
			add(scrollPane2);
			
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
			
			scrollPane2.setFont(new Font("",1,15));
			scrollPane2.setBounds(10, 190, 310, 180);
			
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
					Cache_Content[i][j]="";
			for(int i=0;i<=9;i++)
				for(int j=1;j<=2;j++)
					Mem_Content[i][j]="";
			setVisible(false);
			setVisible(true);
			
		}
		
		
		//cacheEvent true 写回　false　无效
		public void cacheState(String address,boolean cacheEvent){
			
			int addnum = Integer.parseInt(address);
			
			int select=4;
			for(int i=0;i<4;i++){
				if(Cache_Content[i][2].equals(address)) {
					select=i;break;
				}
			}
			if(select!=4){
				if(cacheEvent){//写回
					Cache_Content[select][1]="写回";
					//Cache_Content[select][2]="";
					
					System.out.printf("  %d write back, status: shared\n",addnum);
				}
				else{//无效
					Cache_Content[select][1]="";
					Cache_Content[select][2]="";
					
					System.out.printf("  %d status: invalid\n",addnum);
				}
			}
		}
		
		//directoryEvent true 写缺失　false读缺失
		
		int saveaddnum = 0;
		
        public void directoryState(String process_id,int addnum,int saveaddnum,String address,boolean directoryEvent){
        	
        	int pid = Integer.parseInt(process_id);
        	
		    if(Mem_Content[addnum][1].equals("共享")){
		        if(directoryEvent){		//invalid
					if(Mem_Content[addnum][2].indexOf("1")!=-1){		//CPU1
							panel2.cacheState(address,false);
					}
					if(Mem_Content[addnum][2].indexOf("2")!=-1){		//CPU2
						panel3.cacheState(address,false);
					}
					if(Mem_Content[addnum][2].indexOf("3")!=-1){		//CPU3
						panel4.cacheState(address,false);
					}
					if(Mem_Content[addnum][2].indexOf("4")!=-1){		//CPU4
						panel5.cacheState(address,false);
					}
                    Mem_Content[addnum][1]="独占";
                    Mem_Content[addnum][2]=process_id;
                    
                    System.out.printf("  %d status: exclusive(CPU%d)\n",saveaddnum,pid);
                }
                else{		//read miss
		            Mem_Content[addnum][2]+=process_id;		//add new CPU
		            
		            System.out.printf("  %d status: shared+(CPU%d)\n",saveaddnum,pid);
                }
            }
            else if(Mem_Content[addnum][1].equals("独占")){
				if(Mem_Content[addnum][2].indexOf("1")!=-1){		//CPU1
					panel2.cacheState(address,true);
				}
				else if(Mem_Content[addnum][2].indexOf("2")!=-1){		//CPU2
					panel3.cacheState(address,true);
				}
				else if(Mem_Content[addnum][2].indexOf("3")!=-1){		//CPU3
					panel4.cacheState(address,true);
				}
				else if(Mem_Content[addnum][2].indexOf("4")!=-1){		//CPU4
					panel5.cacheState(address,true);
				}
                if(directoryEvent){		//write miss
                    Mem_Content[addnum][1]="独占";
                    Mem_Content[addnum][2]=process_id;
                    
                    System.out.printf("  %d status: exclusive(CPU%d)\n",saveaddnum,pid);
                }
                else{		//read miss
                    Mem_Content[addnum][1]="共享";
                    Mem_Content[addnum][2]+=process_id;
                    
                    System.out.printf("  %d status: shared+(CPU%d)\n",saveaddnum,pid);
                }
            }
            else {
                if(directoryEvent){		//write miss
                    Mem_Content[addnum][1]="独占";
                    Mem_Content[addnum][2]=process_id;
                    
                    System.out.printf("  %d status: exclusive(CPU%d)\n",saveaddnum,pid);
                }
                else{		//read miss
                    Mem_Content[addnum][1]="共享";
                    Mem_Content[addnum][2]+=process_id;
                    
                    System.out.printf("  %d status: shared+(CPU%d)\n",saveaddnum,pid);
                }
            }
        }
        
        int tag = 0;
        
		public void exProcess(Mypanel process){
			int Method=Mylistmodel1_1.getSelectedIndex();
			int WR=process.Mylistmodel.getSelectedIndex();
			String address=process.jtext.getText();

			if(WR==-1||address==null||Method==-1){
				System.out.println("error,no wr or address or method");
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
							if(process.Cache_Content[select*2][2]==""){
								select=select*2;
							}else if(process.Cache_Content[select*2+1][2]==""){
								select=select*2+1;
							}
							else{
								int tmp=(int)(Math.random()*10)%2;
								select=select*2+tmp;
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
								if(process.Cache_Content[i][2]==""){
									select=i;
									break;
								}
							}
							if(select==4){
								select=(int)(Math.random()*10)%4;
							}
						}
						break;
					default:
						System.out.println("无组相联！");
				}
                String process_id="0";
				if(process==panel2) process_id="1";
				else if(process==panel3) process_id="2";
				else if(process==panel4) process_id="3";
				else if(process==panel5) process_id="4";
				Mypanel objProcess;
				
				saveaddnum = addnum;
				
                if(addnum<10){
                    objProcess=panel2;
                }
                else if(addnum<20){
                  objProcess=panel3;addnum-=10;
                }
                else if(addnum<30){
                  objProcess=panel4;addnum-=20;
                }
                else {
                   objProcess=panel5;addnum-=30;
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
							if(objProcess.Mem_Content[addnum][1].equals("共享")){
                        		objProcess.directoryState(process_id,addnum,saveaddnum,address,true);
							}
                            process.Cache_Content[select][1]="写";
                            process.Cache_Content[select][2]=address;
                            break;
                    }
                    
                    if(tag == 2) {
                		if(WR == 0) {
                			System.out.printf("* read %d\n",saveaddnum);
                			tag = 0;
                			jtext.setEnabled(true);
        	        		Mylistmodel.setEnabled(true);
        	        		nbutton.setEnabled(true);
                		}
                		else if(WR == 1 && objProcess.Mem_Content[addnum][1].equals("共享")) {
                			System.out.printf("* %d exclusive\n",saveaddnum);
                			tag++;
                		}
                		else if(WR == 1 && objProcess.Mem_Content[addnum][1].equals("独占")) {
                			System.out.printf("* write %d\n",saveaddnum);
                			tag = 0;
                			jtext.setEnabled(true);
        	        		Mylistmodel.setEnabled(true);
        	        		nbutton.setEnabled(true);
                		}
                		return;
                	}
                    if(tag == 3) {
                		if(WR == 1) {
                			System.out.printf("* write %d\n",saveaddnum);
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
                		if(WR == 0)	System.out.printf("* read miss, send miss(%d)\n",saveaddnum);
                		else if(WR == 1)	System.out.printf("* write miss, send miss(%d)\n",saveaddnum);
                		tag++;
                		return;
                	}
	                    if(tag == 3) {
	                		if(WR == 0)	System.out.printf("* mem send %d to local\n",saveaddnum);
	                		else if(WR == 1)	System.out.printf("* send %d to local\n",saveaddnum);
	                		tag++;
	                		return;
	                	}
                	
                	if(process.Cache_Content[select][2]!=""){
						int memAdd=Integer.parseInt(process.Cache_Content[select][2]);
						Mypanel CobjProcess;
						if(memAdd<10){
							CobjProcess=panel2;
						}
						else if(memAdd<20){
							CobjProcess=panel3;memAdd-=10;
						}
						else if(addnum<30){
							CobjProcess=panel4;memAdd-=20;
						}
						else {
							CobjProcess=panel5;memAdd-=30;
						}
                		if(objProcess.Mem_Content[memAdd][1].equals("独占")){		//E
							CobjProcess.Mem_Content[memAdd][1]="";
							CobjProcess.Mem_Content[memAdd][2]="";
							
							if(tag == 4)	System.out.printf("  %d writeback\n",saveaddnum);
						}
						else{
							if(objProcess.Mem_Content[memAdd][2].length()==1){
								CobjProcess.Mem_Content[memAdd][1]="";
								CobjProcess.Mem_Content[memAdd][2]="";
							}
							else{
								CobjProcess.Mem_Content[memAdd][2]=CobjProcess.Mem_Content[memAdd][2].replace(process_id,"");
							}

						}
					}
					switch (WR){
						case 0:		//read miss
							objProcess.directoryState(process_id,addnum,saveaddnum,address,false);
							process.Cache_Content[select][1]="读";
							process.Cache_Content[select][2]=address;
							break;
						case 1:		//write miss
							objProcess.directoryState(process_id,addnum,saveaddnum,address,true);
							process.Cache_Content[select][1]="写";
							process.Cache_Content[select][2]=address;
							break;
					}
				}
                
                if(tag == 4) {
            		if(WR == 0)	System.out.printf("* read %d\n",saveaddnum);
            		else if(WR == 1)	System.out.printf("* write %d, status: exclusive\n",saveaddnum);
            		tag = 0;
            		jtext.setEnabled(true);
	        		Mylistmodel.setEnabled(true);
	        		nbutton.setEnabled(true);
            		return;
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
		JFrame myjf = new JFrame("多cache一致性模拟之目录法");
		myjf.setSize(1500, 600);
		myjf.setLayout(null);
		myjf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container C1 = myjf.getContentPane();
		
		/*****新建panel1*****/
		JPanel panel1 = new JPanel();

		C1.add(panel2);
		C1.add(panel3);
		C1.add(panel4);
		C1.add(panel5);
		panel2.setBounds(10, 100, 350, 400);
		panel3.setBounds(350, 100, 350, 400);
		panel4.setBounds(700, 100, 350, 400);
		panel5.setBounds(1050, 100, 350, 400);
		
		
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
		
		
		panel2.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory1");
		panel3.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory2");
		panel4.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory3");
		panel5.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory4");
		
		for(int i=0;i<10;i++){
			panel3.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel2.Mem_Content[i][0])+10));
			panel4.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel2.Mem_Content[i][0])+20));
			panel5.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel2.Mem_Content[i][0])+30));
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
