package com.googlecode.csse460_2010.client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUI extends JFrame implements UIFactory {

	private static final long serialVersionUID = 1L;
	private javax.swing.JPanel jContentPane = null;
	private javax.swing.JPanel jPanel = null;
	private javax.swing.JButton jBShow = null;
	private javax.swing.JButton jBGo = null;
	private javax.swing.JButton jBAttack = null;
	private javax.swing.JButton jBLearn = null;
	private javax.swing.JButton jBHelp = null;
	private javax.swing.JButton jBBye = null;
	private javax.swing.JScrollPane jScrollPane = null;
	private javax.swing.JTextArea jTextArea = null;

	/**
	 * This method initializes
	 * 
	 */
	public GUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(
					5, 5, 5, 5));
		}
		return jContentPane;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setSize(480, 284);
		this.setTitle(XMLParser.getClientMsg("guiTitle"));
		this
				.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				doExit();
			}
		});
		this.setVisible(true);
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new javax.swing.JPanel();
			jPanel.add(getJBShow(), null);
			jPanel.add(getJBGo(), null);
			jPanel.add(getJBAttack(), null);
			jPanel.add(getJBLearn(), null);
			jPanel.add(getJBHelp(), null);
			jPanel.add(getJBBye(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jBShow
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJBShow() {
		if (jBShow == null) {
			jBShow = new javax.swing.JButton();
			jBShow.setText("Show");
			jBShow.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
				}
			});
		}
		return jBShow;
	}

	/**
	 * This method initializes jBGo
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJBGo() {
		if (jBGo == null) {
			jBGo = new javax.swing.JButton();
			jBGo.setText("Move");
			jBGo.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// TODO
				}
			});
		}
		return jBGo;
	}

	/**
	 * This method initializes jBAttack
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJBAttack() {
		if (jBAttack == null) {
			jBAttack = new javax.swing.JButton();
			jBAttack.setText("Fight");
			jBAttack.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String atk = getUserGlbInput(XMLParser
							.getClientMsg("askAtk"));
					Client.processClientInput("fight " + atk);
				}
			});
		}
		return jBAttack;
	}

	/**
	 * This method initializes jBLearn
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJBLearn() {
		if (jBLearn == null) {
			jBLearn = new javax.swing.JButton();
			jBLearn.setText("Learn");
			jBLearn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String atk = getUserGlbInput(XMLParser
							.getClientMsg("askLearn"));
					Client.processClientInput("learn " + atk);
				}
			});
		}
		return jBLearn;
	}

	/**
	 * This method initializes jBHelp
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJBHelp() {
		if (jBHelp == null) {
			jBHelp = new javax.swing.JButton();
			jBHelp.setText("Help");
			jBHelp.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String atk = getUserGlbInput(XMLParser
							.getClientMsg("askHelp"));
					Client.processClientInput("help " + atk);
				}
			});
		}
		return jBHelp;
	}

	/**
	 * This method initializes jBBye
	 * 
	 * @return javax.swing.JButton
	 */

	private javax.swing.JButton getJBBye() {
		if (jBBye == null) {
			jBBye = new javax.swing.JButton();
			jBBye.setText("Exit");
			jBLearn.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doExit();
				}
			});
		}
		return jBBye;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private javax.swing.JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new javax.swing.JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private javax.swing.JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new javax.swing.JTextArea();
			jTextArea.setEditable(false);
			jTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					if (e.isAltDown()
							&& e.getKeyChar() == java.awt.event.KeyEvent.VK_C) {
						// TODO cheat!
					}
				}
			});
		}
		return jTextArea;
	}

	/**
	 * This method is called when the client wishes to quit the game.
	 */
	private void doExit() {
		int n = JOptionPane.showConfirmDialog(null, XMLParser
				.getClientMsg("confirmExit"), XMLParser
				.getClientMsg("guiTitle"), JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(null, XMLParser
					.getClientMsg("quit"), XMLParser
					.getClientMsg("guiTitle"),
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	@Override
	public void mcMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg, XMLParser
				.getClientMsg("mcTitle"), JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void stdMsg(String msg) {
		jTextArea.append(msg + "\n");
		jTextArea.setCaretPosition(jTextArea.getText().length());
	}

	@Override
	public void getNSendCmdInput(String title) {
		/*
		 * We don't do anything here because the command input is done with the
		 * buttons of the GUI.
		 */
	}

	@Override
	public void errMsg(String msg) {
		JOptionPane.showMessageDialog(this, msg, XMLParser
				.getClientMsg("errTitle"), JOptionPane.ERROR_MESSAGE);

	}

	@Override
	public String getUserGlbInput(String title) {
		String rtn = null;
		do {
			rtn = JOptionPane.showInputDialog(title);
		} while (rtn == null || rtn.equals(""));
		return rtn;
	}

}
