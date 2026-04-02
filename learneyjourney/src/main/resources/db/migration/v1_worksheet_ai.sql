-- public.billing_transactions definition

-- Drop table

-- DROP TABLE public.billing_transactions;

CREATE TABLE public.billing_transactions (
	id uuid NOT NULL,
	created_at timestamp(6) NOT NULL,
	created_by varchar(255) NULL,
	updated_at timestamp(6) NULL,
	updated_by varchar(255) NULL,
	amount int4 NOT NULL,
	course_id varchar(255) NULL,
	credits_added int4 NULL,
	status varchar(30) NOT NULL,
	stripe_session_id varchar(255) NULL,
	"type" varchar(30) NOT NULL,
	user_id varchar(255) NOT NULL,
	CONSTRAINT billing_transactions_pkey PRIMARY KEY (id),
	CONSTRAINT billing_transactions_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying])::text[]))),
	CONSTRAINT billing_transactions_type_check CHECK (((type)::text = ANY ((ARRAY['CREDIT_PURCHASE'::character varying, 'SUBSCRIPTION'::character varying, 'COURSE_PURCHASE'::character varying, 'BOOKING_PURCHASE'::character varying])::text[])))
);


-- public.billing_transactions foreign keys


-- public.credit_wallets definition

-- Drop table

-- DROP TABLE public.credit_wallets;

CREATE TABLE public.credit_wallets (
	id uuid NOT NULL,
	created_at timestamp(6) NOT NULL,
	created_by varchar(255) NULL,
	updated_at timestamp(6) NULL,
	updated_by varchar(255) NULL,
	credits int4 NOT NULL,
	user_id varchar(255) NOT NULL,
	CONSTRAINT credit_wallets_pkey PRIMARY KEY (id),
	CONSTRAINT ukywugbn6gv3cjy32me0wqokhp UNIQUE (user_id)
);




-- DROP TABLE public.stripe_transfer;

CREATE TABLE public.stripe_transfer (
	id uuid NOT NULL,
	created_at timestamptz(6) NOT NULL,
	created_by varchar(255) NULL,
	updated_at timestamptz(6) NULL,
	updated_by varchar(255) NULL,
	amount int8 NULL,
	currency varchar(255) NULL,
	order_id uuid NULL,
	purchase_id uuid NULL,
	status varchar(255) NULL,
	stripe_account_id varchar(255) NULL,
	stripe_transfer_id varchar(255) NULL,
	tutor_profile_id uuid NULL,
	CONSTRAINT stripe_transfer_pkey PRIMARY KEY (id)
);

-- public.subscriptions definition

-- Drop table

-- DROP TABLE public.subscriptions;

CREATE TABLE public.subscriptions (
	id uuid NOT NULL,
	created_at timestamp(6) NOT NULL,
	created_by varchar(255) NULL,
	updated_at timestamp(6) NULL,
	updated_by varchar(255) NULL,
	active bool NOT NULL,
	end_date timestamptz(6) NOT NULL,
	plan_name varchar(50) NOT NULL,
	start_date timestamptz(6) NOT NULL,
	stripe_subscription_id varchar(255) NULL,
	user_id varchar(255) NOT NULL,
	CONSTRAINT subscriptions_pkey PRIMARY KEY (id)
);




-- public.tutor_application_subjects definition

-- Drop table

-- DROP TABLE public.tutor_application_subjects;

CREATE TABLE public.tutor_application_subjects (
	application_id uuid NOT NULL,
	subject_name varchar(255) NULL,
	CONSTRAINT tutor_application_subjects_subject_name_check CHECK (((subject_name)::text = ANY ((ARRAY['MATH'::character varying, 'ENGLISH'::character varying, 'SCIENCE'::character varying, 'MUSIC'::character varying, 'ART'::character varying, 'CODING'::character varying, 'LANGUAGES'::character varying, 'BUSINESS'::character varying])::text[])))
);




-- public.tutor_profile_general_availability definition

-- Drop table

-- DROP TABLE public.tutor_profile_general_availability;

CREATE TABLE public.tutor_profile_general_availability (
	tutor_profile_id uuid NOT NULL,
	"day" varchar(255) NULL,
	"time" varchar(255) NULL
);


-- public.tutor_review definition

-- Drop table

-- DROP TABLE public.tutor_review;

CREATE TABLE public.tutor_review (
	id uuid NOT NULL,
	created_at timestamptz(6) NOT NULL,
	created_by varchar(255) NULL,
	updated_at timestamptz(6) NULL,
	updated_by varchar(255) NULL,
	is_visible bool NOT NULL,
	rating int4 NOT NULL,
	review_text text NULL,
	booking_id uuid NOT NULL,
	student_id varchar(255) NOT NULL,
	tutor_id uuid NOT NULL,
	CONSTRAINT tutor_review_pkey PRIMARY KEY (id),
	CONSTRAINT uk_tutor_review_booking UNIQUE (booking_id)
);


-- Drop table

-- DROP TABLE public.worksheet_versions;

CREATE TABLE public.worksheet_versions (
	id uuid NOT NULL,
	created_at timestamp(6) NOT NULL,
	created_by varchar(255) NULL,
	updated_at timestamp(6) NULL,
	updated_by varchar(255) NULL,
	export_count int4 NOT NULL,
	html_content text NOT NULL,
	sort_order int4 NOT NULL,
	version_label varchar(20) NOT NULL,
	worksheet_id uuid NOT NULL,
	CONSTRAINT worksheet_versions_pkey PRIMARY KEY (id)
);




-- public.worksheets definition

-- Drop table

-- DROP TABLE public.worksheets;

CREATE TABLE public.worksheets (
	id uuid NOT NULL,
	created_at timestamp(6) NOT NULL,
	created_by varchar(255) NULL,
	updated_at timestamp(6) NULL,
	updated_by varchar(255) NULL,
	active_version_label varchar(20) NULL,
	is_deleted bool NOT NULL,
	export_count int4 NOT NULL,
	"language" varchar(20) NOT NULL,
	prompt_text text NOT NULL,
	subject varchar(100) NOT NULL,
	title varchar(255) NOT NULL,
	user_id varchar(255) NOT NULL,
	CONSTRAINT worksheets_pkey PRIMARY KEY (id)
);


-- public.worksheets foreign keys

ALTER TABLE public.worksheets ADD CONSTRAINT fkdmftxhgtc6a2omp7sf41cwa24 FOREIGN KEY (user_id) REFERENCES public.users(id);

-- public.worksheet_versions foreign keys

ALTER TABLE public.worksheet_versions ADD CONSTRAINT fk8r6ep85uko1odvjfkrktuxbcg FOREIGN KEY (worksheet_id) REFERENCES public.worksheets(id);

CREATE INDEX idx_tutor_review_created_at ON public.tutor_review USING btree (created_at);
CREATE INDEX idx_tutor_review_student_id ON public.tutor_review USING btree (student_id);
CREATE INDEX idx_tutor_review_tutor_id ON public.tutor_review USING btree (tutor_id);


-- public.tutor_review foreign keys

ALTER TABLE public.tutor_review ADD CONSTRAINT fk1wc62q6ol9q3pmbj6iereabxm FOREIGN KEY (booking_id) REFERENCES public.booking(id);
ALTER TABLE public.tutor_review ADD CONSTRAINT fk3qtcb38yugnagowl3yshf8xam FOREIGN KEY (tutor_id) REFERENCES public.tutor_profile(id);
ALTER TABLE public.tutor_review ADD CONSTRAINT fkfhj7fpv5gj8wdk6640lrbuq5b FOREIGN KEY (student_id) REFERENCES public.users(id);

-- public.worksheet_versions definition

-- public.tutor_profile_general_availability foreign keys

ALTER TABLE public.tutor_profile_general_availability ADD CONSTRAINT fksscjd5vjx2jc6n5sk2dbo0gd5 FOREIGN KEY (tutor_profile_id) REFERENCES public.tutor_profile(id);

-- public.tutor_application_subjects foreign keys

ALTER TABLE public.tutor_application_subjects ADD CONSTRAINT fk90i6myak4kidltqyy9xlo6h8p FOREIGN KEY (application_id) REFERENCES public.tutor_profile(id);

-- public.subscriptions foreign keys

ALTER TABLE public.subscriptions ADD CONSTRAINT fkhro52ohfqfbay9774bev0qinr FOREIGN KEY (user_id) REFERENCES public.users(id);

-- public.credit_wallets foreign keys

ALTER TABLE public.credit_wallets ADD CONSTRAINT fk3w94yauokhmok5a3cmx0yco2f FOREIGN KEY (user_id) REFERENCES public.users(id);
-- public.stripe_transfer definition

-- Drop table

ALTER TABLE public.billing_transactions ADD CONSTRAINT fkbts84h7hrbuuielr18cx1rhg0 FOREIGN KEY (user_id) REFERENCES public.users(id);