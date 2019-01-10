import java.awt.BorderLayout;  
import java.awt.Dimension;  
import java.awt.Color;  
import java.awt.event.ActionEvent;  
import java.awt.event.ActionListener;  
import java.awt.event.ItemEvent;  
import java.awt.event.ItemListener;  
import java.io.File;  
import javax.swing.*;  
import javax.swing.border.EtchedBorder;  
import java.lang.*;  
import java.util.*;  
  
  
public class CCacheSim extends JFrame implements ActionListener {  
    private static final long serialVersionUID = 1L;  
 
    private JPanel panelTop, panelLeft, panelRight, panelBottom;  
    private JButton execStepBtn, execAllBtn, fileBotton;  
    private JComboBox<String> csBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;  
    private JComboBox<String> icsBox, dcsBox;  
    private JFileChooser fileChooser;  
      
    private JLabel labelTop,labelLeft,rightLabel,bottomLabel,fileLabel,fileAddrBtn, stepLabel1, stepLabel2, csLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel,newlineLabel,spaceLeft,spaceTop,spaceRight,spaceOther;  
    private JLabel icsLabel, dcsLabel;  
    private JLabel resultTagLabel[][];  
    private JLabel resultDataLabel[][];  
  
    private JLabel accessTypeTagLabel, addressTagLabel, blockNumberTagLabel, tagTagLabel, indexTagLabel, inblockAddressTagLabel, hitTagLabel;  
    private JLabel accessTypeDataLabel, addressDataLabel, blockNumberDataLabel, tagDataLabel, indexDataLabel, inblockAddressDataLabel, hitDataLabel;  
  
    private JRadioButton unifiedCacheButton;  
 
    private final String cachesize[] = { "2KB", "8KB", "32KB", "128KB", "512KB", "2MB" };  
    private final String scachesize[] = { "1KB", "4KB", "16KB", "64KB", "256KB", "1MB" };  
    private final  String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };  
    private final  String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };  
    private final  String replace[] = { "LRU", "FIFO", "RAND" };  
    private final  String pref[] = { "不预取", "不命中预取" };  
    private final  String write[] = { "写回法", "写直达法" };  
    private final  String alloc[] = { "按写分配", "不按写分配" };    
  
    private final  String resultTags[][] = {  
        {"访问总次数:", "不命中次数:", "不命中率:"},   
        {"读指令次数:", "不命中次数:", "不命中率:"},  
        {"读数据次数:", "不命中次数:", "不命中率:"},  
        {"写数据次数:", "不命中次数:", "不命中率:"}  
    };  
  
    private File file;  
  
 
    private int csIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;  //选项index
  
   
    /**********************************************INSTRUCTION************************************************/
    
    private class Instruction {  
        int opt;  //操作码
        int tag;  
        int index;  
        int blockAddr;  
        int inblockAddr;  //address is presented as "tag + index + inblockAddr"
        
        String addr;
        
        public Instruction(int opt, String addr) {  
            this.opt = opt;  
            this.addr = addr;  
  
            String baddr = this.HtoB(); //转换10进制到2进制 
  
             
                this.tag = Integer.parseInt(baddr.substring(0, 32 - myCache.blockOff - myCache.groupOff), 2);  
                this.index = Integer.parseInt(baddr.substring(32 - myCache.blockOff - myCache.groupOff, 32 - myCache.blockOff), 2);  
                this.blockAddr = Integer.parseInt(baddr.substring(0, 32 - myCache.blockOff), 2);  
                this.inblockAddr = Integer.parseInt(baddr.substring(32 - myCache.blockOff), 2);  
            
           
        }  
  
        private String HtoB() {  //转换10进制到32位2进制地址
            StringBuffer sbuffer = new StringBuffer();  
            int zero = 8 - this.addr.length();  
            for (int i = 0; i < zero; i++) {  
                sbuffer.append("0000");  //补齐32位前面的0
            }  
            for (int i = 0; i < this.addr.length(); i++) {  
                switch(this.addr.charAt(i)) {  //处理成32位0-1地址
                    case '0':  sbuffer.append("0000");  break;  
                    case '1':  sbuffer.append("0001");  break;  
                    case '2':  sbuffer.append("0010");  break;  
                    case '3':  sbuffer.append("0011");  break;  
                    case '4':  sbuffer.append("0100");  break;  
                    case '5':  sbuffer.append("0101");  break;  
                    case '6':  sbuffer.append("0110");  break;  
                    case '7':  sbuffer.append("0111");  break;  
                    case '8':  sbuffer.append("1000");  break;  
                    case '9':  sbuffer.append("1001");  break;  
                    case 'a':  sbuffer.append("1010");  break;  
                    case 'b':  sbuffer.append("1011");  break;  
                    case 'c':  sbuffer.append("1100");  break;  
                    case 'd':  sbuffer.append("1101");  break;  
                    case 'e':  sbuffer.append("1110");  break; 
                    case 'f':  sbuffer.append("1111");  break;  
                    default:  System.out.println("Data Error!");  
                }  
            }  
            return sbuffer.toString();  
        }  
  
    }  
    
    private Instruction instructions[];  
    private final int INSTRUCTION_MAX_SIZE = 2000000;  
    private int isize;  
    private int ip;  
    
    /**********************************************CACHE BLOCK************************************************/
  
    private class CacheBlocks {  
        int tag;  
        boolean dirty;  
        int LRUtime;  //for LRU replace
        long FIFOtime;  
  
        public CacheBlocks(int tag) {  //初始化CACHEBLOCK方法
            this.tag = tag;  
            dirty = false;  
            LRUtime = 0;  
            FIFOtime = -1;  
        }  
    }  
    
    /**********************************************TOOL FUNCTIONS*********************************************/
    
    private int pow(int x, int p) {  
        return (int)Math.pow(x, p);  
    }  
  
    private int log2(int x) {  
        return (int)(Math.log(x) / Math.log(2));  
    }  
    
    /*************************************************CACHE***************************************************/  
    
    private class Cache {   
 
        private CacheBlocks cache[][];  
        
        private int     cacheSize;
        private int     blockSize;   
        private int     blockNum;  //= cacheSize / blockSize
        private int     blockOff;  //= log2(blockSize)
        private int     blockInGroup;  //= pow(2,wayIndex);
        private int     groupNum;  //= blockNum / blockInGroup
        private int     groupOff;  //= log2(groupNum)
        //32bit addr：tag + index + inblockAddr
        
        private long groupFIFO[];  //record FIFOtime
        private int		number;  //for LRU
  
        public Cache(int csize, int bsize) {  //初始化CACHE方法
            cacheSize = csize;  
            blockSize = bsize;  
  
            blockNum = cacheSize / blockSize;  
            blockOff = log2(blockSize);  
  
            blockInGroup = pow(2, wayIndex);	//0,1,2,…… 对应 直接映像,2路,4路……
            groupNum = blockNum / blockInGroup;  
            groupOff = log2(groupNum);  
            
            number = 0;
  
            cache = new CacheBlocks[groupNum][blockInGroup];  	//按组数和块数分配
  
            for (int i = 0; i < groupNum; i++) {  
                for (int j = 0; j < blockInGroup; j++) {  
                    cache[i][j] = new CacheBlocks(-1);  
                }  
            }  
            groupFIFO = new long[groupNum];  
        }  
  
        public boolean read(int tag, int index, int inblockAddr) {  //read cache
            for (int i = 0; i < blockInGroup; i++) {  
                if (cache[index][i].tag == tag) {	//hit  
                    cache[index][i].LRUtime = number;  //更新该cache的LRU时间
                    number++;
                    return true;  
                }  
            }  
            return false;  
        }  
  
        public boolean write(int tag, int index, int inblockAddr) {  //write cache
            for (int i = 0; i < blockInGroup; i++) {  
                if (cache[index][i].tag == tag) {	//hit                    
                    cache[index][i].LRUtime = number;    //更新该cache的LRU时间
                    number++;
                    
                    cache[index][i].dirty = true;  //变为脏块
                    
                    if (writeIndex == 0) {	//写回法 
                    	
                    }
                    else if (writeIndex == 1) {	//写直达  
                        memWTime++;  
                        cache[index][i].dirty = false;  //擦除脏位   
                    }  
                    return true;  
                }  
            }  
            return false;  
        }  
  
        public void prefetch(int nextBlockAddr) {    //预取下一个
            int nextTag = nextBlockAddr / pow(2, groupOff + blockOff);  
            int nextIndex = nextBlockAddr / pow(2, blockOff) % pow(2, groupOff);  
            replaceCacheBlock(nextTag, nextIndex);  
        }  
  
        public void replaceCacheBlock(int tag, int index) {  //replace strategy
            if (replaceIndex == 0) {	//LRU  
                int lruBlock = 0;  
                for (int i = 1; i < blockInGroup; i++) {  
                    if (cache[index][lruBlock].LRUtime > cache[index][i].LRUtime) {  
                        lruBlock = i;
                    }  
                }  
                loadToCache(tag, index, lruBlock);  
            }
            else if (replaceIndex == 1) {	//FIFO  
                int fifoBlock = 0;  
                for (int i = 1; i < blockInGroup; i++) {  
                    if (cache[index][fifoBlock].FIFOtime > cache[index][i].FIFOtime) {  
                        fifoBlock = i;
                    }  
                }  
                loadToCache(tag, index, fifoBlock);  
            }
            else if (replaceIndex == 2) {    //random  
                int ranBlock = (int)Math.random() * (blockInGroup);
                loadToCache(tag, index, ranBlock);  
            }  
        }  
  
        private void loadToCache(int tag, int index, int groupAddr) {  //将写回法有脏位的写到MEM中，并把新的tag load到找好替换位置的cache中
            if (writeIndex == 0 && cache[index][groupAddr].dirty) {  //写回法 而且是脏的
                	//替换前写回;  
                memWTime++;  
            }  
  
            cache[index][groupAddr].LRUtime = number;  
            cache[index][groupAddr].FIFOtime = groupFIFO[index];  
            
            cache[index][groupAddr].tag = tag;  
            cache[index][groupAddr].dirty = false;  
            groupFIFO[index]++;  
        }  
    }  
  
    Cache myCache;
  

    /*******************************************INSTRUCTION COUNTERS*******************************************/
  
    private int rdMissTime, riMissTime, riHitTime, rdHitTime;  
    private int wdHitTime, wdMissTime;  
    private int memWTime;  
    private int preTime;
  

    private class DinFileFilter extends  javax.swing.filechooser.FileFilter{  //过滤出.din类型的文件
        public boolean accept(File f) {  
            if (f.isDirectory()) return true;  
            String name = f.getName();  
            return name.endsWith(".din") || name.endsWith(".DIN");  
        }  
  
        public String getDescription() {  
            return ".din";  
        }  
    }  
   
    public CCacheSim(){  
        super("Cache 模拟器");  
        fileChooser = new JFileChooser();  
        fileChooser.setFileFilter(new DinFileFilter());  //用上面的方法过滤出.din文件
        draw();  
    }  
    
    /*********************************************ACTION PERFORMED*********************************************/
  
    public void actionPerformed(ActionEvent e) {  
        if (e.getSource() == execAllBtn) {  //执行到底
            simExecAll();  
        }  
        if (e.getSource() == execStepBtn) {  //执行单步
            simExecStep(true);  
        }  
        if (e.getSource() == fileBotton){  //文件按钮
            int fileOver = fileChooser.showOpenDialog(null);  
            if (fileOver == 0) {  
                    String path = fileChooser.getSelectedFile().getAbsolutePath();  
                    fileAddrBtn.setText(path);  
                    file = new File(path);                  
  
                    initCache();  
                    readFile();  
                    reloadUI();  
            }  
        }  
    }  
  
    /*******************************************INITIALIZE CACHE********************************************/  
    private void initCache() {  

        rdMissTime = 0;  
        riMissTime = 0;  
        rdHitTime = 0;  
        riHitTime = 0; 
        wdHitTime = 0;  
        wdMissTime = 0;  
        memWTime = 0;  
        preTime = 0;
        myCache = new Cache(2 * 1024 * pow(4, csIndex), 16 * pow(2, bsIndex));  //初始化Cache
    }  
      
    /*******************************************READ FROM FILE**********************************************/ 
    
    private void readFile() {  
        try {  
            Scanner s = new Scanner(file);  
            instructions = new Instruction[INSTRUCTION_MAX_SIZE];  //把指令全都读到这里
            isize = 0;  
            ip = 0;  //set ip pointer to zero
  
            while(s.hasNextLine()) {  
                String line = s.nextLine();  
                String[] items0 = line.split(" ");  
                String[] items1 = items0[1].split("\t");
                String[] items = new String[2];
                
                items[0] = items0[0];
                items[1] = items1[0];
                
                instructions[isize] = new Instruction(Integer.parseInt(items[0].trim()), items[1].trim());
                
                isize++;  
            }  
        } catch(Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    private void reloadUI() {  
        for (int i = 0; i < 4; i++) {  
            for (int j = 0; j < 2; j++) {  
                resultDataLabel[i][j].setText("0");  
            }  
            resultDataLabel[i][2].setText("0.00%");  
        }  
    }  
      
    /*单步执行 */  
    private void simExecStep(boolean oneStepExec) {    
        if (ip == 0) {  //初始化
            initCache();  
            reloadUI();  
        }  
        
        int opt = instructions[ip].opt;  
        int index = instructions[ip].index;  
        int tag = instructions[ip].tag;  
        int inblockAddr = instructions[ip].inblockAddr;  
  
        boolean isHit = false;  

        /*********************************************Decoder***********************************************/
        
            if (opt == 0) {	// read data  
                isHit = myCache.read(tag, index, inblockAddr);  //true: 命中 false:不命中
                if (isHit) {  
                    rdHitTime++;  
                }
                else {  
                    rdMissTime++;  
                    	//find the block in memory
                    myCache.replaceCacheBlock(tag, index);  
                    	//load the data in block into CPU 
                }  
            }
            else if (opt == 1) {	// write data  
                isHit = myCache.write(tag, index, inblockAddr);  //true: 命中 false:不命中
                if (isHit) {  
                    wdHitTime++;  
                }
                else {  
                    wdMissTime++;  
                    	//find the block in memory  
                    if (allocIndex == 0) {		//按写分配  
                        	//load target block into Cache 
                        myCache.replaceCacheBlock(tag, index);  
                        	//write into the loaded Cache block 
                        myCache.write(tag, index, inblockAddr);  //不命中这里要重写
                    }
                    else if (allocIndex == 1) {	//不按写分配
                        	//not load the missed block into Cache, only write to memory  
                        memWTime++;  
                    }  
                }  
  
  
            }
            else if (opt == 2) {	// read instruction   
                isHit = myCache.read(tag, index, inblockAddr);  
                if (isHit) {  
                    riHitTime++;  
                }
                else {  
                    riMissTime++;  
                    	//此处在MEM中找对应
                    myCache.replaceCacheBlock(tag, index);  
                    	//读到CPU中 
                    if (prefetchIndex == 0) {	//不预取 
                        //doing nothing  
                    }
                    else if (prefetchIndex == 1){	//预取下一条指令
                    	preTime++;
                        myCache.prefetch(instructions[ip].blockAddr + 1);  
                    }  
                }  
            }  
  
        if (oneStepExec || ip == isize - 1) {  //只剩一步或是单步执行时
            statisticUIUpdate(instructions[ip], isHit);  
        }
        
        ip++;  //自动指向下一条指令
    }  
    
    /*执行到底*/  
    private void simExecAll() {  //以执行单步的方式执行到底
        while (ip < isize) {  
            simExecStep(false);  
        }  
    }  
    
    public static void main(String[] args) {  
        new CCacheSim();  
    }  
  
    private void statisticUIUpdate(Instruction inst, boolean isHit) {  //更新数据显示
  
        int totalMissTime = riMissTime + rdMissTime + wdMissTime;  
        int totalVisitTime = totalMissTime + riHitTime + rdHitTime + wdHitTime + preTime;   
          
        resultDataLabel[0][0].setText(totalVisitTime + "");  //访问总次数
        resultDataLabel[0][1].setText(totalMissTime + "");  //命中次数
        if (totalVisitTime > 0) {  
            double missRate = ((double)totalMissTime / (double)totalVisitTime) * 100;  
            resultDataLabel[0][2].setText(String.format("%.2f", missRate) + "%");  //命中率
        }  
  
        resultDataLabel[1][0].setText((riHitTime + riMissTime + preTime) + "");  //读指令
        resultDataLabel[1][1].setText(riMissTime + "");  
        if (riMissTime + riHitTime > 0) {  
            double missRate = ((double)riMissTime/(double)(riMissTime + riHitTime + preTime)) * 100;  
            resultDataLabel[1][2].setText(String.format("%.2f", missRate) + "%");  
        }  
  
        resultDataLabel[2][0].setText((rdHitTime + rdMissTime) + "");  //读数据
        resultDataLabel[2][1].setText(rdMissTime + "");  
        if (rdMissTime + rdHitTime > 0) {  
            double missRate = ((double)rdMissTime / (double)(rdMissTime + rdHitTime)) * 100;  
            resultDataLabel[2][2].setText(String.format("%.2f", missRate) + "%");  
        }  
          
        resultDataLabel[3][0].setText((wdHitTime + wdMissTime) + "");  //写数据
        resultDataLabel[3][1].setText(wdMissTime + "");  
        if (wdMissTime + wdHitTime > 0) {  
            double missRate = ((double)wdMissTime / (double)(wdMissTime + wdHitTime)) * 100;  
            resultDataLabel[3][2].setText(String.format("%.2f", missRate) + "%");  
        }  
    }   
  
    /**************************************************DRAW*******************************************************/
  
    private void draw() {  
        setLayout(new BorderLayout(5,5));  
        panelTop = new JPanel();  
        panelLeft = new JPanel();  
        panelRight = new JPanel();  
        panelBottom = new JPanel();
        panelLeft.setPreferredSize(new Dimension(300, 450));  
        panelRight.setPreferredSize(new Dimension(500, 450));  
        panelBottom.setPreferredSize(new Dimension(800, 100));  
        panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));  
        panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));  
        panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));  
        panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));  
        
		labelTop = new JLabel("Cache Simulator");
		labelTop.setAlignmentX(CENTER_ALIGNMENT);
		spaceTop = new JLabel(" ");
		spaceTop.setPreferredSize(new Dimension(800, 40));
		
		panelTop.add(labelTop);
		panelTop.add(spaceTop);
 
        labelLeft = new JLabel("Cache 参数设置");  
        labelLeft.setAlignmentX(CENTER_ALIGNMENT);
        spaceLeft = new JLabel(" ");	//占行
        spaceLeft.setPreferredSize(new Dimension(300, 20));  
  
        csLabel = new JLabel("总大小");  
        csLabel.setPreferredSize(new Dimension(120, 30));  
        csBox = new JComboBox<String>(cachesize);  
        csBox.setPreferredSize(new Dimension(160, 30));  
        csBox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) {  
                csIndex = csBox.getSelectedIndex();  
            }  
        });  
  
        //cache 块大小设置  
        bsLabel = new JLabel("块大小");  
        bsLabel.setPreferredSize(new Dimension(120, 30));  
        bsBox = new JComboBox<String>(blocksize);  
        bsBox.setPreferredSize(new Dimension(160, 30));  
        bsBox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) {  
                bsIndex = bsBox.getSelectedIndex();  
            }  
        });  
  
        //相连度设置  
        wayLabel = new JLabel("相联度");  
        wayLabel.setPreferredSize(new Dimension(120, 30));  
        wayBox = new JComboBox<String>(way);  
        wayBox.setPreferredSize(new Dimension(160, 30));  
        wayBox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) {  
                wayIndex = wayBox.getSelectedIndex();  
            }  
        });  
          
        //替换策略设置  
        replaceLabel = new JLabel("替换策略");  
        replaceLabel.setPreferredSize(new Dimension(120, 30));  
        replaceBox = new JComboBox<String>(replace);  
        replaceBox.setPreferredSize(new Dimension(160, 30));  
        replaceBox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) {  
                replaceIndex = replaceBox.getSelectedIndex();  
            }  
        });  
          
        //欲取策略设置  
        prefetchLabel = new JLabel("预取策略");  
        prefetchLabel.setPreferredSize(new Dimension(120, 30));  
        prefetchBox = new JComboBox<String>(pref);  
        prefetchBox.setPreferredSize(new Dimension(160, 30));  
        prefetchBox.addItemListener(new ItemListener(){  
            public void itemStateChanged(ItemEvent e){  
                prefetchIndex = prefetchBox.getSelectedIndex();  
            }  
        });  
          
        //写策略设置  
        writeLabel = new JLabel("写策略");  
        writeLabel.setPreferredSize(new Dimension(120, 30));  
        writeBox = new JComboBox<String>(write);  
        writeBox.setPreferredSize(new Dimension(160, 30));  
        writeBox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) {  
                writeIndex = writeBox.getSelectedIndex();  
            }  
        });  
          
        //调块策略  
        allocLabel = new JLabel("写不命中调块策略");  
        allocLabel.setPreferredSize(new Dimension(120, 30));  
        allocBox = new JComboBox<String>(alloc);  
        allocBox.setPreferredSize(new Dimension(160, 30));  
        allocBox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) {  
                allocIndex = allocBox.getSelectedIndex();  
            }  
        });  
          
        //选择指令流文件  
        
        spaceOther = new JLabel(" ");	//占位
        spaceOther.setPreferredSize(new Dimension(300,10));
        
        fileLabel = new JLabel("选择指令流文件");  
        fileLabel.setPreferredSize(new Dimension(120, 30));  
        fileAddrBtn = new JLabel();  
        fileAddrBtn.setPreferredSize(new Dimension(210,30));  
        fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));  
        fileBotton = new JButton("浏览");  
        fileBotton.setPreferredSize(new Dimension(70,30));  
        fileBotton.addActionListener(this);  
  
        panelLeft.add(labelLeft);  
        panelLeft.add(spaceLeft);
   
        panelLeft.add(csLabel);  
        panelLeft.add(csBox);  
  
        panelLeft.add(bsLabel);  
        panelLeft.add(bsBox);  
        panelLeft.add(wayLabel);  
        panelLeft.add(wayBox);  
        panelLeft.add(replaceLabel);  
        panelLeft.add(replaceBox);  
        panelLeft.add(prefetchLabel);  
        panelLeft.add(prefetchBox);  
        panelLeft.add(writeLabel);  
        panelLeft.add(writeBox);  
        panelLeft.add(allocLabel);  
        panelLeft.add(allocBox);  
        
        panelLeft.add(spaceOther);
        panelLeft.add(fileLabel);  
        panelLeft.add(fileAddrBtn);  
        panelLeft.add(fileBotton);  
  
        //*****************************右侧面板绘制*****************************************//
        
        //模拟结果展示区域  
        rightLabel = new JLabel("模拟结果");  
        rightLabel.setAlignmentX(CENTER_ALIGNMENT);
        spaceRight = new JLabel(" ");	//占位
        spaceRight.setPreferredSize(new Dimension(500, 20));  
        
        panelRight.add(rightLabel);  
        panelRight.add(spaceRight);
  
        resultTagLabel = new JLabel[4][3];  
        resultDataLabel = new JLabel[4][3];  
  
        for (int i = 0; i < 4; i++) {  
            for (int j = 0; j < 3; j++) {  
                resultTagLabel[i][j] = new JLabel(resultTags[i][j]);  
                resultTagLabel[i][j].setPreferredSize(new Dimension(70, 40));  
  
                if (j != 2) {  
                    resultDataLabel[i][j] = new JLabel("0");  
                } else {  
                    resultDataLabel[i][j] = new JLabel("0.00%");  
                }  
                  
                resultDataLabel[i][j].setPreferredSize(new Dimension(83, 40));  
  
                panelRight.add(resultTagLabel[i][j]);  
                panelRight.add(resultDataLabel[i][j]);  
            }
            
            if (i == 0) {  
                JLabel label = new JLabel("\n");  //占行
                label.setPreferredSize(new Dimension(500, 40));  
                panelRight.add(label);  
            }  
        }   
  
  
        //*****************************底部面板绘制*****************************************//  
          
        bottomLabel = new JLabel("执行控制");  
		bottomLabel.setAlignmentX(CENTER_ALIGNMENT); 
		newlineLabel = new JLabel(" ");		//占行
		newlineLabel.setPreferredSize(new Dimension(800, 10)); 
        execStepBtn = new JButton("步进");  
        execStepBtn.setLocation(100, 30);  
        execStepBtn.addActionListener(this);  
        execAllBtn = new JButton("执行到底");  
        execAllBtn.setLocation(300, 30);  
        execAllBtn.addActionListener(this);  
          
        panelBottom.add(bottomLabel);  
        panelBottom.add(newlineLabel);
        panelBottom.add(execStepBtn);  
        panelBottom.add(execAllBtn);  
  
        add("North", panelTop);  
        add("West", panelLeft);  
        add("Center", panelRight);  
        add("South", panelBottom);  
        setSize(820, 620);  
        setVisible(true);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
    }  
}  
