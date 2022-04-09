import re
archivo = open("log")
line = [archivo.readline()]
print("EXPRESION REGULAR:")
print(line)

while 1:
    line = re.subn('(((T0)(.*?)((T1)(.*?)(T3)|(T2)(.*?)(T4))(.*?)(T5))|((T6)(.*?)(T7)(.*?)(T8)(.*?)(T9)))',
                   '\g<4>\g<7>\g<10>\g<12>\g<16>\g<18>\g<20>', line[0])
    print(line)
    if line[1] == 0:
        break

if len(line[0]) == 0:
    print('No sobra ninguna transición')
else:
    print('Sobran transiciones')
