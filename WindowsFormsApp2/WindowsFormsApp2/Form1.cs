using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO.Ports;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WindowsFormsApp2
{
    public partial class Form1 : Form
    {
        SerialPort _serialPort = new SerialPort("COM7", 9600, Parity.None, 8, StopBits.One);
        Dictionary<string, Image> classes = new Dictionary<string, Image>()
        {
            {"blank",Properties.Resources.alpha1 },
            {"sword",Properties.Resources.sword },
            {"armour",Properties.Resources.armour },
            {"deckkrash",Properties.Resources.DeckKrash },
            {"eraser",Properties.Resources.eraser },
            {"cya",Properties.Resources.cya },
            {"worm",Properties.Resources.worm },
            {"speedy",Properties.Resources.banhammer},
            {"banhammer",Properties.Resources.banhammer},
            {"shield",Properties.Resources.banhammer}
        };
        prog[] allPrograms = {
            new prog(){name="Blank", attack=0, defence=0, Class="blank", maxRez=0, curRez=0, effect=""},
    new prog(){name="Armor",attack=0,defence=0,Class="armour",maxRez=7,curRez=7,effect="Lowers all brain damage you would receive by 4, as long as this Program remains Rezzed. Only 1 copy of this Program can be running at a time. Each copy of this Program can only be used once per Netrun."},
    new prog() { name="sword",attack=1,defence=0,Class="sword",maxRez=0,curRez=0,effect="Does 3d6 REZ to a Black ICE Program, or 2d6 REZ to a Non-Black ICE Program"},
    new prog() { name="See Ya",attack=0,defence=0,Class="cya",maxRez=7,curRez=7,effect="Increases all Pathfinder Checks you make by +2 as long as this Program remains Rezzed"},
    new prog() { name="worm",attack=0,defence=0,Class="worm",maxRez=7,curRez=7,effect="Increases all Backdoor Checks you make by +2 as long as this Program remains Rezzed"},
    new prog() { name="Banhammer",attack=1,defence=0,Class="banhammer",maxRez=0,curRez=0,effect="Does 3d6 REZ to a Non-Black ICE Program, or 2d6 REZ to a Black-ICE Program"},
    new prog() { name="DeckKRASH",attack=0,defence=0,Class="deckkrash",maxRez=0,curRez=0,effect="Enemy Netrunner is forcibly and unsafely Jacked Out of the Architecture, suffering the effect of all Rezzed enemy Black ICE they've encountered in the Architecture as they leave."},
    new prog() { name="Eraser",attack=0,defence=0,Class="eraser",maxRez=7,curRez=7,effect="Increases all Cloak Checks you make by +2 as long as this Program remains Rezzed."},
    new prog() { name="Flak",attack=0,defence=0,Class="speedy",maxRez=7,curRez=7,effect="Reduces the ATK of all Non-Black ICE Attacker Programs run against you to 0 as long as this Program remains Rezzed. Only 1 copy of this Program can be running at a time. Each copy of this Program can only be used once per Netrun."},
    new prog() { name="Speedy Gonzalvez",attack=0,defence=0,Class="speedy",maxRez=7,curRez=7,effect="Increases your Speed by +2 as long as this Program remains Rezzed."},
    new prog() { name="Shield",attack=0,defence=0,Class="speedy",maxRez=7,curRez=7,effect="Stops the first successful Non-Black ICE Program Effect from dealing brain damage. After stopping this damage, the Shield Derezzes itself. Only 1 copy of this Program can be running at a time. Each copy of this Program can only be used once per Netrun"},
    new prog(){name="HellBolt",attack=2,defence=0,Class="speedy",maxRez=0,curRez=0,effect="Does 2d6 Damage direct to the new prog()enemy Netrunner's brain. Unless insulated, their Cyberdeck catches fire along with their clothing. Until new prog()they spend a Meat Action to put themselves out, they take 2 damage to their HP whenever they end their Turn.new prog() Multiple instances of this effect cannot stack."},
    new prog(){name="Nervescrub",attack=0,defence=0,Class="speedy",maxRez=0,curRez=0,effect="Enemy Netrunner's INT, REF, andnew prog() DEX are each lowered by 1d6 for the next hour (minimum 1). The effects are largely psychosomatic and leave new prog()no permanent effects."},
    new prog(){name="Poison Flatline",attack=0,defence=0,Class="speedy",maxRez=0,curRez=0,effect="Destroys a single Non-new prog()Black ICE Program installed on the Netrunner target's Cyberdeck at random."},
    new prog(){name="Superglue",attack=2,defence=0,Class="speedy",maxRez=0,curRez=0,effect="Enemy Netrunner cannot progress new prog()deeper into the Architecture or Jack Out safely for 1d6 Rounds (enemy Netrunner can still perform an unsafe new prog()Jack Out, though). Each copy of this Program can only be used once per Netrun."},
    new prog() { name="Vrizbolt",attack=1,defence=0,Class="speedy",maxRez=0,curRez=0,effect="Does 1d6 Damage direct to a Netrunner's brain and lowers the amount of total NET Actions the Netrunner can accomplish on their next Turn by 1 (minimum 2)."}

    };

        public Form1()
        {
            InitializeComponent();
            
            _serialPort.Handshake = Handshake.None;
            try
            {

                if (!(_serialPort.IsOpen))
                    _serialPort.Open();
                setLED(1, 0);
                setLED(2, 0);
                setLED(3, 0);
                setLED(4, 0);
                setLED(5, 0);
                _serialPort.DataReceived += new SerialDataReceivedEventHandler(sp_DataReceived);


            }
            catch (Exception ex)
            {
                MessageBox.Show("Error opening/writing to serial port :: " + ex.Message, "Error!");
            }
        }
        private delegate void SetTextDeleg(byte[] text);
        private void si_DataReceived(byte[] data) 
        {
            var port = data[0];
            if (port == 0xff)
            {
                port = 0;
            }
            if(port>allPrograms.Length)
            {
                Console.WriteLine(port);
                port = 0;
            }
            var program = allPrograms[port];
            pictureBox2.Image = classes[program.Class];
        }
        private void sp_DataReceived(object sender, SerialDataReceivedEventArgs e)
        {
            Thread.Sleep(500);
            byte[] buff = new byte[7];
            _serialPort.Read(buff, 0, 7);
            
            // Invokes the delegate on the UI thread, and sends the data that was received to the invoked method.  
            // ---- The "si_DataReceived" method will be executed on the UI thread which allows populating of the textbox.  
            this.BeginInvoke(new SetTextDeleg(si_DataReceived), new object[] { buff });
        }

        int[] states = { 0, 0, 0, 0, 0 };
        Color[] colorStates = { Color.FromArgb(80, 72, 80), Color.Green, Color.Red };
        private void button1_Click(object sender, EventArgs e)
        {
            var state = 
            states[0] = (states[0] + 1) % 3;
            setLED(1, states[0]);
            ((Control)sender).BackColor = colorStates[state];
        }
        private void setLED(int port, int state)
        {
            try
            {
                _serialPort.Write(port.ToString().PadLeft(2, '0') + state);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error opening/writing to serial port :: " + ex.Message, "Error!");
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            var state=
            states[1] = (states[1] + 1) % 3;
            setLED(2, states[1]);
            ((Control)sender).BackColor = colorStates[state];
        }

        private void button3_Click(object sender, EventArgs e)
        {
            var state = 
            states[2] = (states[2] + 1) % 3;
            setLED(3, states[2]);
            ((Control)sender).BackColor = colorStates[state];
        }

        private void button4_Click(object sender, EventArgs e)
        {
            var state = 
            states[3] = (states[3] + 1) % 3;
            setLED(4, states[3]);
            ((Control)sender).BackColor = colorStates[state];
        }

        private void button5_Click(object sender, EventArgs e)
        {
            var state = 
            states[4] = (states[4] + 1) % 3;
            setLED(5, states[4]);
            ((Control)sender).BackColor = colorStates[state];
        }
    }
    public class prog
    {
        public string name = "";
        public int attack = 0;
        public int defence = 0;
        public string Class = "";
        public int maxRez = 0;
        public int curRez = 0;
        public string effect = "";
    }
}
