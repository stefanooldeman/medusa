#!/usr/bin/env python

import csv
import re
import sys
import Stemmer


# constants / cached values
filter_re = re.compile(r'[^A-Za-z0-9\ \-]')

class ParseFromCSV(object):

    def __init__(self, input_csv, lang='english'):
        self.input_csv = input_csv
        self.language = lang
        self.stemmer = Stemmer.Stemmer(self.language)
        self.stopwords = self.init_stopwords()

    def to_stdout(self):
        with file(self.input_csv, 'r') as f:
            reader = csv.DictReader(f, delimiter=',')
            for row in reader:
                twotuple = self.parse(row)

    def parse(self, row):
        label = row.get('g:google_product_category').strip()
        terms = row.get('description').strip()
        if terms and label and label.find('>') is not -1:
            words = filter_re.sub('', terms).lower().split(' ')
            # if not lowercased, stopwords won't always be matched
            word_cats = self.removeStopwords(words)
            clean_terms = self.stemmer.stemWords(word_cats)
            # join back the terms for stdout
            return (label, ' '.join(clean_terms))
        # not applicable for parsing..
        return False

    def removeStopwords(self, words):
        def match(word):
            return word not in self.stopwords

        return filter(match, words)

    def init_stopwords(self):
        filename = "stopwords/%s.txt" % self.language 
        words = open(filename, 'r').readlines()
        stopwords = map(lambda x: x.strip(), words)
        return stopwords


if __name__ == "__main__":
    # parse argv
    if len(sys.argv) is not 2:
        raise Exception("Expected two arguments, 1. the csv file")

    _, csv_file = sys.argv
    parser = ParseFromCSV(csv_file, lang='dutch')
    parser.to_stdout()

