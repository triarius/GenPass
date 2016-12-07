import sys
import sqlite3

conn = sqlite3.connect('words.db')
c = conn.cursor()
c.execute('CREATE TABLE words (_id INTEGER PRIMARY KEY, word TEXT NOT NULL, length INTEGER NOT NULL)')

with open(sys.argv[1]) as f:
    words = [x.strip() for x in f.readlines()]
    c.executemany(
        'INSERT INTO words (word, length) VALUES (?,?)',
        [(x, len(x)) for x in words]
    )

c.execute('CREATE INDEX idx_length ON words (length)')

conn.commit()
conn.close()
