CREATE TABLE indexword (
  index_word_id   integer(10),
  lemma           varchar(255),
  pos             char
);

CREATE INDEX i_indexword ON indexword (pos, lemma(10));

CREATE TABLE synset (
  synset_id       integer(10),
  file_offset     integer(10),
  lex_file_num    integer(10),
  pos             char,
  is_adj_cluster  bit,
  gloss           text
);

CREATE INDEX i_synset ON synset (pos, file_offset);

CREATE TABLE synsetword (
  synset_word_id  integer(10),
  synset_id       integer(10),
  word            varchar(255),
  word_index      integer(10),
  usage_cnt	      integer(10),
  lex_id	      integer(10)
);

CREATE INDEX i_synsetword On synsetword (synset_id);

CREATE TABLE synsetpointer (
  synset_pointer_id integer(10),
  synset_id         integer(10),
  pointer_type      varchar(2),
  target_offset     integer(10),
  target_pos        char,
  source_index      integer(10),
  target_index      integer(10)
);

CREATE INDEX i_synsetpointer On synsetpointer (synset_id);
  
CREATE TABLE synsetverbframe (
  synset_verb_frame_id  integer(10),
  synset_id             integer(10),
  frame_number          integer(10),
  word_index            integer(10)
);

CREATE INDEX i_synsetverbframe On synsetverbframe (synset_id);

CREATE TABLE indexwordsynset (
  index_word_synset_id  integer(10),
  index_word_id         integer(10),
  synset_id             integer(10)
);

CREATE INDEX i_indexwordsynset On indexwordsynset (index_word_id, synset_id);

CREATE TABLE exceptions (
  exception_id    integer(10),
  pos             char,
  base            varchar(255),
  derivation      varchar(255)
);

CREATE INDEX i_exception On exceptions (pos, base(10));