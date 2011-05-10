/*!
 * Copyright © 2010 Sattvik Software & Technology Resources, Ltd. Co.
 * All rights reserved.
 *
 * sh-clojure may be used under the terms of either the GNU Lesser General Public
 * License (LGPL) or the Eclipse Public License (EPL).  As a recipient of
 * sh-clojure, you may choose which license to receive the code under.  See the
 * LICENSE file distributed with sh-clojure for details.
 *
 * Written by Daniel Solano Gómez
 *
 * Version 0.9.1 - 10 Apr 2010
 */

function ClojureRegExp(pattern) {
	pattern = pattern + '(?=[[\\]{}(),\\s])';
	this.regex = new RegExp(pattern, 'g');
	this.lookBehind = /[\[\]{}(),\s]$/;
}

ClojureRegExp.prototype.exec = function (str) {
	var match, leftContext;
	while (match=this.regex.exec(str)) {
		leftContext = str.substring(0, match.index);
		if (this.lookBehind.test(leftContext)) {
			return match;
		}
		else {
			this.regex.lastIndex = match.index + 1;
		}
	}
	return null;
};

SyntaxHighlighter.brushes.Clojure = function () {
	var special_forms =
			'. def do fn if let loop monitor-enter monitor-exit new quote recur set! ' +
			'throw try var',
	    clojure_core =
			'* *1 *2 *3 *agent* *allow-unresolved-vars* *assert* *clojure-version* ' +
			'*command-line-args* *compile-files* *compile-path* *e *err* *file* ' +
			'*flush-on-newline* *in* *macro-meta* *math-context* *ns* *out* ' +
			'*print-dup* *print-length* *print-level* *print-meta* *print-readably* ' +
			'*read-eval* *source-path* *use-context-classloader* ' +
			'*warn-on-reflection* + - -> -&gt; ->> -&gt;&gt; .. / < &lt; <= &lt;= = ' +
			'== > &gt; >= &gt;= accessor aclone ' +
			'add-classpath add-watch agent agent-errors aget alength alias all-ns ' +
			'alter alter-meta! alter-var-root amap ancestors and apply areduce ' +
			'array-map aset aset-boolean aset-byte aset-char aset-double aset-float ' +
			'aset-int aset-long aset-short assert assoc assoc! assoc-in associative? ' +
			'atom await await-for await1 bases bean bigdec bigint binding bit-and ' +
			'bit-and-not bit-clear bit-flip bit-not bit-or bit-set bit-shift-left ' +
			'bit-shift-right bit-test bit-xor boolean boolean-array booleans ' +
			'bound-fn bound-fn* butlast byte byte-array bytes cast char char-array ' +
			'char-escape-string char-name-string char? chars chunk chunk-append ' +
			'chunk-buffer chunk-cons chunk-first chunk-next chunk-rest chunked-seq? ' +
			'class class? clear-agent-errors clojure-version coll? comment commute ' +
			'comp comparator compare compare-and-set! compile complement concat cond ' +
			'condp conj conj! cons constantly construct-proxy contains? count ' +
			'counted? create-ns create-struct cycle dec decimal? declare definline ' +
			'defmacro defmethod defmulti defn defn- defonce defstruct delay delay? ' +
			'deliver deref derive descendants destructure disj disj! dissoc dissoc! ' +
			'distinct distinct? doall doc dorun doseq dosync dotimes doto double ' +
			'double-array doubles drop drop-last drop-while empty empty? ensure ' +
			'enumeration-seq eval even? every? false? ffirst file-seq filter find ' +
			'find-doc find-ns find-var first float float-array float? floats flush ' +
			'fn fn? fnext for force format future future-call future-cancel ' +
			'future-cancelled? future-done? future? gen-class gen-interface gensym ' +
			'get get-in get-method get-proxy-class get-thread-bindings get-validator ' +
			'hash hash-map hash-set identical? identity if-let if-not ifn? import ' +
			'in-ns inc init-proxy instance? int int-array integer? interleave intern ' +
			'interpose into into-array ints io! isa? iterate iterator-seq juxt key ' +
			'keys keyword keyword? last lazy-cat lazy-seq let letfn line-seq list ' +
			'list* list? load load-file load-reader load-string loaded-libs locking ' +
			'long long-array longs loop macroexpand macroexpand-1 make-array ' +
			'make-hierarchy map map? mapcat max max-key memfn memoize merge ' +
			'merge-with meta method-sig methods min min-key mod name namespace neg? ' +
			'newline next nfirst nil? nnext not not-any? not-empty not-every? not= ' +
			'	ns ns-aliases ns-imports ns-interns ns-map ns-name ns-publics ' +
			'ns-refers ns-resolve ns-unalias ns-unmap nth nthnext num number? odd? ' +
			'or parents partial partition pcalls peek persistent! pmap pop pop! ' +
			'pop-thread-bindings pos? pr pr-str prefer-method prefers ' +
			'primitives-classnames print print-ctor print-doc print-dup print-method ' +
			'print-namespace-doc print-simple print-special-doc print-str printf ' +
			'println println-str prn prn-str promise proxy proxy-call-with-super ' +
			'proxy-mappings proxy-name proxy-super push-thread-bindings pvalues quot ' +
			'rand rand-int range ratio? rational? rationalize re-find re-groups ' +
			're-matcher re-matches re-pattern re-seq read read-line read-string ' +
			'reduce ref ref-history-count ref-max-history ref-min-history ref-set ' +
			'refer refer-clojure release-pending-sends rem remove remove-method ' +
			'remove-ns remove-watch repeat repeatedly replace replicate require ' +
			'reset! reset-meta! resolve rest resultset-seq reverse reversible? rseq ' +
			'rsubseq second select-keys send send-off seq seq? seque sequence ' +
			'sequential? set set-validator! set? short short-array shorts ' +
			'shutdown-agents slurp some sort sort-by sorted-map sorted-map-by ' +
			'sorted-set sorted-set-by sorted? special-form-anchor special-symbol? ' +
			'split-at split-with str stream? string? struct struct-map subs subseq ' +
			'subvec supers swap! symbol symbol? sync syntax-symbol-anchor take ' +
			'take-last take-nth take-while test the-ns time to-array to-array-2d ' +
			'trampoline transient tree-seq true? type unchecked-add unchecked-dec ' +
			'unchecked-divide unchecked-inc unchecked-multiply unchecked-negate ' +
			'unchecked-remainder unchecked-subtract underive unquote ' +
			'unquote-splicing update-in update-proxy use val vals var-get var-set ' +
			'var? vary-meta vec vector vector? when when-first when-let when-not ' +
			'while with-bindings with-bindings* with-in-str with-loading-context ' +
			'with-local-vars with-meta with-open with-out-str with-precision xml-seq ' +
			'zero? zipmap ';

	this.getKeywords = function (keywordStr) {
		// quote special characters
		keywordStr = keywordStr.replace(/[\-\[\]{}()*+?.\\\^$|,#]/g, "\\$&");
		// trim whitespace and convert to alternatives
		keywordStr = keywordStr.replace(/^\s+|\s+$/g, '').replace(/\s+/g, '|');
		// create pattern
		return '(?:' + keywordStr + ')';
	};

	this.regexList = [
		// comments
		{ regex: new RegExp(';.*$', 'gm'),
			css: 'comments' },
		// strings
		{ regex: SyntaxHighlighter.regexLib.multiLineDoubleQuotedString,
			css: 'string' },
		// regular expressions
		{ regex: /#"(?:\.|(\\\")|[^\""\n])*"/g,
			css: 'string' },
		// vectors
		{ regex: /\[|\]/g,
			css: 'keyword' },
		// amperstands
		{ regex: /&(amp;)?/g,
			css: 'keyword' },
		// sets and maps
		{ regex: /#?\{|\}/g,
			css: 'keyword' },
		// metadata
		{ regex: /#\^\{/g,
			css: 'keyword' },
		// anonymous fn syntactic sugar
		{ regex: /#\(|%/g,
			css: 'keyword' },
		// deref reader macro
		{ regex: /@/g,
			css: 'keyword' },
		// (un)quoted sexprs
		{ regex: /(['`]|~@?)[\[({]/g,
			css: 'keyword' },
		// lists
		{ regex: /\(|\)/g,
			css: 'keyword' },
		// character literals
		{ regex: /\\.\b/g,
			css: 'value' },
		// hexadecimal literals
		{ regex: /[+\-]?\b0x[0-9A-F]+\b/gi,
			css: 'value' },
		// integer/octal/float/bigdecimal literals
		{ regex: new ClojureRegExp("[+-]?\\b\\d+(\\.\\d*)?([eE][+-]?\\d+|M)?\\b"),
			css: 'value' },
		{ regex: /^[+\-]?\b\d+(\.\d*)?([eE][+\-]?\d+|M)?\b/g,
			css: 'value' },
		// booleans+nil
		{ regex: /\b(true|false|nil)\b/g,
			css: 'value' },
		// (un)quoted symbols
		{ regex: /(`|#?'|~@?)[\w-.\/]+/g,
			css: 'color1' },
		// keywords
		{ regex: /:[A-Za-z0-9_\-]+/g,
			css: 'constants' },
		// special forms
		{ regex: new ClojureRegExp(this.getKeywords(special_forms)),
			css: 'preprocessor' },
		// type hints
		{ regex: /\#\^[A-Za-z]\w*/g,
			css: 'preprocessor' },
		// clojure.core
		{ regex: new ClojureRegExp(this.getKeywords(clojure_core)),
			css: 'functions' }
	];

	this.forHtmlScript(SyntaxHighlighter.regexLib.scriptScriptTags);
};

SyntaxHighlighter.brushes.Clojure.prototype = new SyntaxHighlighter.Highlighter();
SyntaxHighlighter.brushes.Clojure.aliases   = ['clojure', 'Clojure', 'clj'];

// vim: ts=2 sw=2 noet
