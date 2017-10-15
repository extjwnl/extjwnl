CREATE TABLE indexword (
  index_word_id   integer,
  lemma           varchar(255),
  pos             char
);

CREATE INDEX i_indexword ON indexword (pos, lemma);

CREATE TABLE synset (
  synset_id       integer,
  file_offset     integer,
  lex_file_num    integer,
  pos             char,
  is_adj_cluster  boolean,
  gloss           text
);

CREATE INDEX i_synset ON synset (pos, file_offset);

CREATE TABLE synsetword (
  synset_word_id  integer,
  synset_id       integer,
  word            varchar(255),
  word_index      integer,
  usage_cnt	      integer,
  lex_id	        integer
);

CREATE INDEX i_synsetword ON synsetword (synset_id);

CREATE TABLE synsetpointer (
  synset_pointer_id integer,
  synset_id         integer,
  pointer_type      varchar(2),
  target_offset     integer,
  target_pos        char,
  source_index      integer,
  target_index      integer
);

CREATE INDEX i_synsetpointer ON synsetpointer (synset_id);
  
CREATE TABLE synsetverbframe (
  synset_verb_frame_id  integer,
  synset_id             integer,
  frame_number          integer,
  word_index            integer
);

CREATE INDEX i_synsetverbframe ON synsetverbframe (synset_id);

CREATE TABLE indexwordsynset (
  index_word_synset_id  integer,
  index_word_id         integer,
  synset_id             integer,
  synset_rank           integer
);

CREATE INDEX i_indexwordsynset ON indexwordsynset (index_word_id, synset_id, synset_rank);

CREATE TABLE exceptions (
  exception_id    integer,
  pos             char,
  base            varchar(255),
  derivation      varchar(255)
);

CREATE INDEX i_exception ON exceptions (pos, base);