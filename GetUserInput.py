from datetime import datetime, date, time
from __future__ import print_function # For py 2.7 compat

from IPython.html import widgets # Widget definitions
from IPython.display import display # Used to display widgets in the notebook
from IPython.utils.traitlets import Unicode # Used to declare attributes of our widget

from dateutil import parser
from pytz import timezone
import pytz

#variables
userinput = "userinput.csv"
pacific = timezone('US/Pacific')
stringdate = datetime.now(pacific).isoformat(' ').split('.')[0]


#TextBox
txtBox = widgets.Text()
txtBox.value = stringdate

display(txtBox)

def on_value_change(name, value):
    global stringdate
    
    try:
        parser.parse(value) 
        stringdate = value
    except:
        stringdate = datetime.now(pacific).isoformat(' ').split('.')[0]

txtBox.on_trait_change(on_value_change, 'value')


#Button

button = widgets.ButtonWidget(description="Change Date")
display(button)

def on_button_clicked(b):
    open(userinput,"w").write(stringdate)
    txtBox.value = stringdate

button.on_click(on_button_clicked)
