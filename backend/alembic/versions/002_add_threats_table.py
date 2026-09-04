"""Add Threats Table

Revision ID: 002_add_threats_table
Revises: 001_initial_schema
Create Date: 2026-09-02 10:00:00.000000

"""
from typing import Sequence, Union
from alembic import op
import sqlalchemy as sa

revision: str = '002_add_threats_table'
down_revision: Union[str, None] = '001_initial_schema'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None

def upgrade() -> None:
    op.create_table(
        'threats',
        sa.Column('id', sa.String(length=36), nullable=False),
        sa.Column('category', sa.String(length=50), nullable=False),
        sa.Column('severity', sa.Float(), nullable=False, server_default='5.0'),
        sa.Column('title', sa.String(length=150), nullable=False),
        sa.Column('description', sa.String(length=500), nullable=True),
        sa.Column('latitude', sa.Float(), nullable=False),
        sa.Column('longitude', sa.Float(), nullable=False),
        sa.Column('radius', sa.Float(), nullable=False, server_default='500.0'),
        sa.Column('timestamp', sa.DateTime(timezone=True), nullable=False),
        sa.Column('source', sa.String(length=50), nullable=True, server_default='SYSTEM'),
        sa.Column('confidence', sa.Float(), nullable=False, server_default='0.85'),
        sa.Column('is_active', sa.Boolean(), nullable=False, server_default='1'),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_threats_category'), 'threats', ['category'], unique=False)

def downgrade() -> None:
    op.drop_index(op.f('ix_threats_category'), table_name='threats')
    op.drop_table('threats')
