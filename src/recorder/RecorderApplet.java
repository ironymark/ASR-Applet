package recorder;

import javax.swing.JApplet;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import netscape.javascript.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ButtonGroup;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;

import javax.sound.sampled.LineUnavailableException;

import javax.sound.sampled.TargetDataLine;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RecorderApplet extends JApplet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField tbReuslt2;
	private JTextField tbReuslt1;
	private JTextField tbReuslt3;
	private final ButtonGroup buttonGroup = new ButtonGroup();
    protected boolean running;
    ByteArrayOutputStream out;
    JButton btnStart;
    String pid;
    final String MSG_SELECT_ONE="Select the best match and click 'Use' or try again.";
    final String MSG_ERR="Please try again.";
    final String MSG_PROCESSING="Processing...";
    final String MSG_CLICK_START="Click Start to begin.";
    JLabel lblSelect;
	public RecorderApplet() {}
	   public void init() {
			setSize(300, 200);

	        //Execute a job on the event-dispatching thread; creating this applet's GUI.
	        try {
	            SwingUtilities.invokeAndWait(new Runnable() {
	                public void run() {
	                    createGUI();
	                }
	            });
	        } catch (Exception e) { 
	            System.err.println("createGUI didn't complete successfully");
	        }		
	   }
	    private void createGUI() {
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			getContentPane().add(panel, BorderLayout.CENTER);
			panel.setLayout(null);
			
			btnStart = new JButton("Start");
			btnStart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String label = btnStart.getText();
					if("Start".equals(label)){
						lblSelect.setText(MSG_PROCESSING);
						btnStart.setText("Stop");
						captureAudio();
					}else{
						btnStart.setText("Start");
						stopCapture();
					}
					
				}
			});
			btnStart.setBounds(6, 6, 65, 23);
			panel.add(btnStart);
			
			JLabel lblAudioLevel = new JLabel("Audio Level:");
			lblAudioLevel.setBounds(73, 10, 85, 14);
			panel.add(lblAudioLevel);
			
			lblSelect = new JLabel(MSG_CLICK_START);
			lblSelect.setBounds(6, 40, 280, 14);
			panel.add(lblSelect);
			
			JLabel lblResult = new JLabel("Results");
			lblResult.setBounds(6, 65, 46, 14);
			panel.add(lblResult);
			
			JRadioButton rbResult1 = new JRadioButton("");
			buttonGroup.add(rbResult1);
			rbResult1.setActionCommand("1");
			rbResult1.setBackground(Color.WHITE);
			rbResult1.setBounds(6, 86, 21, 23);
			panel.add(rbResult1);
			
			JRadioButton rbResult2 = new JRadioButton("");
			buttonGroup.add(rbResult2);
			rbResult2.setActionCommand("2");
			rbResult2.setBackground(Color.WHITE);
			rbResult2.setBounds(6, 112, 21, 23);
			panel.add(rbResult2);
			
			JRadioButton rbResult3 = new JRadioButton("");
			buttonGroup.add(rbResult3);
			rbResult3.setActionCommand("3");
			rbResult3.setBackground(Color.WHITE);
			rbResult3.setBounds(6, 138, 21, 23);
			panel.add(rbResult3);
			
			tbReuslt2 = new JTextField();
			tbReuslt2.setBounds(33, 115, 248, 20);
			panel.add(tbReuslt2);
			tbReuslt2.setColumns(10);
			
			tbReuslt1 = new JTextField();
			tbReuslt1.setColumns(10);
			tbReuslt1.setBounds(33, 90, 248, 20);
			panel.add(tbReuslt1);
			
			tbReuslt3 = new JTextField();
			tbReuslt3.setColumns(10);
			tbReuslt3.setBounds(33, 138, 248, 20);
			panel.add(tbReuslt3);
			
			JButton btnUse = new JButton("Use");
			btnUse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String actionCmd = buttonGroup.getSelection().getActionCommand();
					String curSel = "";
		            if("1".equals(actionCmd))
		            	curSel = tbReuslt1.getText();
		            if("2".equals(actionCmd))
		            	curSel = tbReuslt2.getText();
		            if("3".equals(actionCmd))
		            	curSel = tbReuslt3.getText();
		            if(!"".equals(curSel)){
		            	SetResponse(curSel);
		            	closeASRDlg();
		            }
		            
		            
				}
			});
			btnUse.setBounds(55, 169, 80, 23);
			panel.add(btnUse);
			
			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					closeASRDlg();
				}
			});
			btnCancel.setBounds(140, 169, 80, 23);
			panel.add(btnCancel);
			panel.validate();
			setContentPane(panel);
			validate();
		}
//	   public void paint( Graphics g ) {
//		   super.paint(g);
//	      g.fillRect(160, 10, 100, 16);
//	      invalidate();
//	   }	   
	   public void stopCapture() {

			running = false;
			try {
				byte audio[] = out.toByteArray();
				InputStream input = new ByteArrayInputStream(audio);
				final AudioFormat format = getFormat();
				final AudioInputStream ais = new AudioInputStream(input, format,
						audio.length / format.getFrameSize());
				//String strFilename = "c:/temp/captureFile3.wav";
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);

				// recorder.stopCapture();
				//System.out.println("Capture stopped.");
				String charset = "UTF-8";
				URLConnection urlConnection;
				String servletURL = this.getParameter("servletURL");
				urlConnection = new URL(
						servletURL+"?pid="+pid)
						.openConnection();

				urlConnection.setConnectTimeout(20000); // long timeout, but not
														// infinite
				urlConnection.setReadTimeout(20000);
				urlConnection.setUseCaches(false);
				urlConnection.setDoOutput(true); // Triggers POST.
				urlConnection.setRequestProperty("accept-charset", charset);
				urlConnection.setRequestProperty("content-type",
						"binary/octet-stream");
				OutputStream os = urlConnection.getOutputStream();

				os.write(baos.toByteArray());
				os.flush();
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream()));
				String xml;
				StringBuffer sb = new StringBuffer();
				while ((xml = rd.readLine()) != null) {
					sb.append(xml);
					//System.out.println(xml);
				}
				rd.close();
				os.close();
				parseResponse(sb.toString());
				//SetResponse(sb.toString());

			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	private void parseResponse(String xml) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
	        Document doc = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
	        doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("ERROR_CODE");
            Node fstNode = nodeLst.item(0);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                Element fstElmnt = (Element) fstNode;
                NodeList fstNm = fstElmnt.getChildNodes();
                String err = ((Node) fstNm.item(0)).getNodeValue();
                //System.out.println(err);
                if(!"0".equals(err)){
                	lblSelect.setText(MSG_ERR);
                	return;
                }
            }
            ArrayList<String> results = new ArrayList<String>(); 
            for(int i=1;i<4;i++){
                nodeLst = doc.getElementsByTagName("HYP"+i);
                if(nodeLst!=null && nodeLst.getLength()>0){
	                fstNode = nodeLst.item(0);
	                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
	                    Element fstElmnt = (Element) fstNode;
	                    NodeList fstNm = fstElmnt.getChildNodes();
	                    String hyp = ((Node) fstNm.item(0)).getNodeValue();
	                    //System.out.println(hyp);
	                    results.add(hyp);
	                }
                }            	
            }
            int resSize = results.size();
            if(resSize>0)
            	tbReuslt1.setText(results.get(0));
            if(resSize>1)
            	tbReuslt2.setText(results.get(1));
            if(resSize>2)
            	tbReuslt3.setText(results.get(2));
            if(resSize>0)
            	lblSelect.setText(MSG_SELECT_ONE);
            else
            	lblSelect.setText(MSG_ERR);
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
		protected int calculateRMSLevel(byte[] audioData)
	    { // audioData might be buffered data read from a data line
	        long lSum = 0;
	        for(int i=0; i<audioData.length; i++)
	            lSum = lSum + audioData[i];
	 
	        double dAvg = lSum / audioData.length;
	 
	        double sumMeanSquare = 0d;
	        for(int j=0; j<audioData.length; j++)
	            sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);
	 
	        double averageMeanSquare = sumMeanSquare / audioData.length;
	        return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
	    }    
	    public void captureAudio() {
	        try {
	          final AudioFormat format = getFormat();
	          DataLine.Info info = new DataLine.Info(
	            TargetDataLine.class, format);
	          final TargetDataLine line = (TargetDataLine)
	            AudioSystem.getLine(info);
	          line.open(format);
	          line.start();
	          Runnable runner = new Runnable() {
	            int bufferSize = (int)format.getSampleRate() 
	              * format.getFrameSize();
	            byte buffer[] = new byte[bufferSize];
	     
	            public void run() {
	              out = new ByteArrayOutputStream();
	              running = true;
	              try {
	                while (running) {
	                  int count = 
	                    line.read(buffer, 0, buffer.length);
		              	//float level = line.getLevel();
	                  int level = calculateRMSLevel(buffer);
	                  drawLeveBar(level);
		            	//System.out.println(level);
	                  if (count > 0) {
	                    out.write(buffer, 0, count);
	                  }
	                }
	                out.close();
	                line.drain();
	                line.close();                
	              } catch (IOException e) {
	                System.err.println("I/O problems: " + e);
	                System.exit(-1);
	              }
	            }
	          };
	          Thread captureThread = new Thread(runner);
	          captureThread.start();
	        } catch (LineUnavailableException e) {
	          System.err.println("Line unavailable: " + e);
	          System.exit(-2);
	        }
	      }
	    private AudioFormat getFormat() {
	        float sampleRate = 16000;
	        int sampleSizeInBits = 16;
	        int channels = 1;
	        int frameSize = 2;
	        float frameRate = 16000;
	        boolean bigEndian = true;
       
	        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,sampleRate, sampleSizeInBits,channels, frameSize, frameRate, bigEndian);
	      }
		public void SetResponse(String string) {
		       JSObject win = (JSObject) JSObject.getWindow(this);
		       win.call("setASRText", new String[] {string});			   
		}
		public void closeASRDlg() {
		       JSObject win = (JSObject) JSObject.getWindow(this);
		       win.call("closeASRDlg", null);			   
		}
		
		public void setPid(String pid){
			this.pid = pid; 
		}
		private void drawLeveBar(int level) {
			Graphics g = getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(160, 10, 100, 16);
			g.setColor(Color.BLACK);
			g.fillRect(160, 10, level, 16);
			
			
		}		
}