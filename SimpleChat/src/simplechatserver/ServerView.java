package simplechatserver;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class ServerView extends JFrame implements StandardParameters {

	private static final long serialVersionUID = 1L;
	private Server server = null;
	private Document doc = null;
	
	public ServerView(int serverPort) {
		super("Chat Server");
		this.server = new Server(serverPort, this);
		Container pane = getContentPane();
        pane.setLayout(new BorderLayout());
        JTextPane textPane = new JTextPane();
        textPane.setBorder(new TitledBorder("Messages"));
        textPane.setEditable(false);
        doc = textPane.getDocument();
        try {
            doc.insertString(doc.getLength(), "Waiting for Messages\n", null);
        } catch (BadLocationException e) {;}
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(300, 500));
        pane.add(scrollPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ServerView.this.server.stop();
                ServerView.this.dispose();
                System.exit(0);
            }
        });
        pack();
        setVisible(true);
		new Thread(server).start();
	}

	public static void main(String[] args){
		if (args.length != 1){
			new ServerView(PORT);
		}
		else {
			new ServerView(Integer.parseInt(args[0]));
		}
	}
	
	public void display(String str) {
		final String message = str;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	if (ServerView.this.doc == null) return;
                try {
                    ServerView.this.doc.insertString(
                            ServerView.this.doc.getLength(),
                            message,
                            null);
                } catch (BadLocationException e) {;}
            }
        });
	}

}
