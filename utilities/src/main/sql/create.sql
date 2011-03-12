CREATE TABLE IndexWord (
  index_word_id   integer(10),
  lemma           varchar(255),
  pos             char
);

CREATE INDEX I_IndexWord ON IndexWord (pos, lemma(10));

CREATE TABLE Synset (
  synset_id       integer(10),
  file_offset     integer(10),
  lex_file_num    integer(10),
  pos             char,
  is_adj_cluster  bit,
  gloss           text
);

CREATE INDEX I_Synset ON Synset (pos, file_offset);

CREATE TABLE SynsetWord (
  synset_word_id  integer(10),
  synset_id       integer(10),
  word            varchar(255),
  word_index      integer(10),
  usage_cnt	      integer(10),
  lex_id	      integer(10)
);

CREATE INDEX I_SynsetWord On SynsetWord (synset_id);

CREATE TABLE SynsetPointer (
  synset_pointer_id integer(10),
  synset_id         integer(10),
  pointer_type      varchar(2),
  target_offset     integer(10),
  target_pos        char,
  source_index      integer(10),
  target_index      integer(10)
);

CREATE INDEX I_SynsetPointer On SynsetPointer (synset_id);
  
CREATE TABLE SynsetVerbFrame (
  synset_verb_frame_id  integer(10),
  synset_id             integer(10),
  frame_number          integer(10),
  word_index            integer(10)
);

CREATE INDEX I_SynsetVerbFrame On SynsetVerbFrame (synset_id);

CREATE TABLE IndexWordSynset (
  index_word_synset_id  integer(10),
  index_word_id         integer(10),
  synset_id             integer(10)
);

CREATE INDEX I_IndexWordSynset On IndexWordSynset (index_word_id, synset_id);

CREATE TABLE Exceptions (
  exception_id    integer(10),
  pos             char,
  base            varchar(255),
  derivation      varchar(255)
);

CREATE INDEX I_Exception On Exceptions (pos, base(10));