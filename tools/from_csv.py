#!/usr/bin/env python

import csv
import re
import sys


filter_re = re.compile(r'[^A-Za-z0-9\ \-]')


if len(sys.argv) is not 2:
   raise Exception("Expected two arguments, 1. the csv file")

name, csv_file = sys.argv
reader = csv.DictReader(file(csv_file, 'r'), delimiter=',')

for item in reader:
    uid = item.get('description').strip()
    category = item.get('g:google_product_category').strip()
    if uid and category and category.find('>') is not -1:
            clean_uid = filter_re.sub('', uid)
            out = '%s|%s\n' % (category, clean_uid)
            #out = category + "\n"
            sys.stdout.write(out)
